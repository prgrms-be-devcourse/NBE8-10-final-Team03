package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;
import com.eof.back.domain.gamesession.dto.GameSessionListResponse;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.GameSessionErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.GameSessionException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
}
