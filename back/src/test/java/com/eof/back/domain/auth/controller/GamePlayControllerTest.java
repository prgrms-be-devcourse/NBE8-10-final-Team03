package com.eof.back.domain.auth.controller;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author 유재원
 * @see
 * @since 2026-03-27
 */

import com.eof.back.domain.gamesession.controller.GamePlayController;
import com.eof.back.domain.gamesession.dto.ChatMessageRequest;
import com.eof.back.domain.gamesession.dto.GameMessageResponse;
import com.eof.back.domain.gamesession.dto.QuizAnswerRequest;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.gamesession.service.GamePlayService;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamePlayControllerTest {

    @InjectMocks
    private GamePlayController gamePlayController;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private GamePlayService gamePlayService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @Test
    @DisplayName("채팅 전송 성공 - 브로드캐스트 정상 호출")
    void chat_Success() {
        Long sessionId = 1L;
        String nickname = "testUser";
        String message = "안녕하세요!";

        ChatMessageRequest request = new ChatMessageRequest(message);

        given(authentication.getPrincipal()).willReturn(userPrincipal);
        given(userPrincipal.nickname()).willReturn(nickname);

        gamePlayController.chat(sessionId, request, authentication);

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/rooms/1/chat"), any(GameMessageResponse.class));
    }

    @Test
    @DisplayName("채팅 전송 실패 - 인증되지 않은 사용자")
    void chat_Fail_Unauthenticated() {
        Long sessionId = 1L;
        ChatMessageRequest request = new ChatMessageRequest("안녕하세요!");

        assertThatThrownBy(() -> gamePlayController.chat(sessionId, request, null))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("사용자 권한 인증에 실패하였습니다.");
    }

    @Test
    @DisplayName("게임 시작 성공 - 서비스 정상 호출")
    void startGame_Success() {
        Long sessionId = 1L;
        String username = "testUser@test.com";

        given(authentication.getPrincipal()).willReturn(userPrincipal);
        given(userPrincipal.username()).willReturn(username);

        gamePlayController.startGame(sessionId, authentication);

        verify(gamePlayService, times(1)).startGame(sessionId);
    }

    @Test
    @DisplayName("게임 시작 실패 - 인증되지 않은 사용자")
    void startGame_Fail_Unauthenticated() {
        Long sessionId = 1L;

        assertThatThrownBy(() -> gamePlayController.startGame(sessionId, null))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("사용자 권한 인증에 실패하였습니다.");
    }

    @Test
    @DisplayName("정답 제출 성공 - 서비스 정상 호출")
    void submitAnswer_Success() {
        Long sessionId = 1L;
        String nickname = "testUser";
        String answer = "정답입니다";

        QuizAnswerRequest request = new QuizAnswerRequest(answer);

        given(authentication.getPrincipal()).willReturn(userPrincipal);
        given(userPrincipal.nickname()).willReturn(nickname);

        gamePlayController.submitAnswer(sessionId, request, authentication);

        verify(gamePlayService, times(1)).submitAnswer(sessionId, nickname, answer);
    }
}