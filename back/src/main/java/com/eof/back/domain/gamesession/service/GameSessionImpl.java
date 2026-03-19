package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.entity.GameSessionStatus;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    @Transactional
    public GameSessionCreateResponse createGameSession(Long userId, GameSessionCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND, "유저가 없습니다"));
        
        QuizSet quizSet = QuizSet.builder().build(); //Todo 퀴즈셋 찾아오기 추가

        GameSession gameSession = GameSession.of(request.roomName(), user, quizSet, request.maxQuizzes(), request.maxPlayers());
        gameSession = gameSessionRepository.save(gameSession);

        return GameSessionCreateResponse.from(gameSession);
    }
}
