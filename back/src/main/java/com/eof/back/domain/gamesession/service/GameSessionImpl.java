package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.*;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.entity.GameSessionStatus;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.GameSessionErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.GameSessionException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import com.eof.back.global.response.CommonResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 게임세션(room) 인터페이스의 기본 구현체입니다.
 *
 * @author 유재원
 * @since 2026-03-18
 */

@Service
@RequiredArgsConstructor
public class GameSessionImpl implements GameSessionService {
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final QuizSetRepository quizSetRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public GameSessionCreateResponse createGameSession(Long userId, GameSessionCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND, "유저가 없습니다"));

        QuizSet quizSet = quizSetRepository.findById(request.quizSetId())
                .orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND, "해당 퀴즈 세트를 찾을 수 없습니다. ID: " + request.quizSetId()));

        GameSession gameSession = GameSession.of(request.roomName(), user, quizSet, request.maxQuizzes(), request.maxPlayers());
        gameSession = gameSessionRepository.save(gameSession);

        return GameSessionCreateResponse.from(gameSession);
    }

    @Override
    @Transactional
    public List<GameSessionListResponse> getGameSessionByRoomName(String roomName) {
        return gameSessionRepository.findByRoomNameContaining(roomName).stream()
                .map(GameSessionListResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public List<GameSessionListResponse> getAllGameSessions() {
        return gameSessionRepository.findAll().stream()
                .map(GameSessionListResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void deleteGameSession(Long userId, Long gameSessionId) {
        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new GameSessionException(GameSessionErrorCode.GAME_SESSION_NOT_FOUND, "해당 게임 세션을 찾을 수 없습니다. ID: " + gameSessionId));
        // 방장(Host)랑 유저가 일치하지 않으면 오류
        if (!gameSession.getHost().getId().equals(userId)) {
            throw new GameSessionException(GameSessionErrorCode.UNAUTHORIZED_HOST_ACTION, "방장이 아닌 사람이 방을 삭제할 수 없습니다.");
        }
        // 검증 후 삭제
        gameSessionRepository.delete(gameSession);
    }

    @Override
    @Transactional
    public GameSessionJoinResponse joinRoom(Long userId, Long gameSessionId) {
        // 1. 들어오는 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND, "유저를 찾을 수 없습니다."));

        // 2. 방 조회
        GameSession gameSession = gameSessionRepository.findByIdWithPlayers(gameSessionId)
                .orElseThrow(() -> new GameSessionException(GameSessionErrorCode.GAME_SESSION_NOT_FOUND, "방을 찾을 수 없습니다."));

        // 3. 방 들어오기 처리
        if (gameSession.getStatus() != GameSessionStatus.WAIT) {
            throw new GameSessionException(GameSessionErrorCode.INVALID_GAME_STATUS, "이미 게임이 시작되었거나 종료된 방입니다.");
        }

        gameSession.join(user);
        // 4. 최신 상태의 방 정보 DTO로 변환
        GameSessionJoinResponse responseData = GameSessionJoinResponse.from(gameSession);

        String joinMessage = user.getNickname() + "님이 입장하셨습니다.";

        GameMessageResponse<GameSessionJoinResponse> response = GameMessageResponse.enter(
                "SYSTEM", // 보낸 사람을 시스템으로 명시
                joinMessage,
                responseData // 최신 방 정보 첨부
        );

        // 채팅 채널로 발송!
        messagingTemplate.convertAndSend("/topic/rooms/" + gameSessionId + "/chat", response);

        return responseData;
    }

    @Override
    @Transactional
    public void leaveRoom(Long userId, Long gameSessionId) {

        // 1. 나가는 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND, "유저를 찾을 수 없습니다."));

        // 2. 방 조회
        GameSession gameSession = gameSessionRepository.findByIdWithPlayers(gameSessionId)
                .orElseThrow(() -> new GameSessionException(GameSessionErrorCode.GAME_SESSION_NOT_FOUND, "방을 찾을 수 없습니다."));

        String destination = "/topic/rooms/" + gameSessionId + "/chat";

        // 3. 방장 여부 확인 및 분기 처리
        if (gameSession.getHost().getId().equals(userId)) {

            // 게임 세션을 삭제하기 전에, 유저 리스트 비우기
            gameSession.getPlayers().clear();
            // [CASE 1] 나가는 사람이 방장인 경우: 방 자체를 DB에서 완전히 삭제
            gameSessionRepository.delete(gameSession);

            // 프론트엔드에게 방이 폭파되었음을 TYPE : ROOM_DELETED으로 알림
            GameMessageResponse<Void> response = GameMessageResponse.roomDeleted("방장이 퇴장하여 게임 방이 삭제되었습니다.");
            messagingTemplate.convertAndSend(destination, response);
        } else {
            // [CASE 2] 일반 참가자가 나가는 경우: 방 명단에서 해당 유저만 제거 및 카운트 감소
            gameSession.leave(user);

            // 방금 1명이 지워진 최신 상태의 방 정보 DTO로 변환
            GameSessionJoinResponse responseData = GameSessionJoinResponse.from(gameSession);

            String leaveMessage = user.getNickname() + "님이 퇴장하셨습니다.";
            GameMessageResponse<GameSessionJoinResponse> response = GameMessageResponse.leave(
                    "SYSTEM",
                    leaveMessage,
                    responseData
            );

            // 통합 채팅 채널로 발송
            messagingTemplate.convertAndSend(destination, response);
        }
    }
}
