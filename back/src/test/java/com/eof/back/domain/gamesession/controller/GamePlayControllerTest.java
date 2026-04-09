package com.eof.back.domain.gamesession.controller;

import com.eof.back.domain.gamesession.dto.ChatMessageRequest;
import com.eof.back.domain.gamesession.dto.QuizAnswerRequest;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.gamesession.service.GamePlayService;
import com.eof.back.global.jwt.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GamePlayControllerTest {

    @InjectMocks
    private GamePlayController gamePlayController;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private GamePlayService gamePlayService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    private Authentication authentication;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "tester", "tester", "USER");
        authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null);
    }

    @Test
    @DisplayName("채팅 메시지 전송 테스트")
    void chat_Success() {
        Long gameSessionId = 1L;
        ChatMessageRequest request = new ChatMessageRequest("안녕하세요");

        gamePlayController.chat(gameSessionId, request, authentication);

        verify(messagingTemplate).convertAndSend(eq("/topic/rooms/1/chat"), any(Object.class));
    }

    @Test
    @DisplayName("게임 시작 테스트")
    void startGame_Success() {
        Long gameSessionId = 1L;

        gamePlayController.startGame(gameSessionId, authentication);

        verify(gamePlayService).startGame(gameSessionId);
    }

    @Test
    @DisplayName("정답 제출 테스트")
    void submitAnswer_Success() {
        Long gameSessionId = 1L;
        QuizAnswerRequest request = new QuizAnswerRequest("정답");

        gamePlayController.submitAnswer(gameSessionId, request, authentication);

        verify(gamePlayService).submitAnswer(gameSessionId, userPrincipal.nickname(), "정답");
    }
}
