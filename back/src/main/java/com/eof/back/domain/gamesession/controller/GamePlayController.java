package com.eof.back.domain.gamesession.controller;

import com.eof.back.domain.gamesession.dto.ChatMessageRequest;
import com.eof.back.domain.gamesession.dto.GameMessageResponse;
import com.eof.back.domain.gamesession.dto.QuizAnswerRequest;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.gamesession.service.GamePlayService;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * 웹소켓(STOMP)을 통해 클라이언트로부터 들어오는 실시간 메시지를 처리하는 컨트롤러입니다.
 * <p>
 * 클라이언트가 특정 경로로 메시지를 발행(Publish)하면, {@code @MessageMapping}을 통해
 * 해당 요청을 가로채어 채팅, 게임 시작, 정답 제출 등의 작업을 수행합니다.
 * 처리된 결과는 {@code SimpMessagingTemplate}을 사용하여 해당 방을 구독(Subscribe) 중인
 * 모든 참가자에게 브로드캐스트(Broadcast)되거나 서비스 계층으로 위임됩니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Controller} 어노테이션이 선언되어 있어 Spring 컨테이너에 의해 빈(Bean)으로 등록 및 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 웹소켓 통신을 위해 Spring WebSocket을 사용하며, 메세지 발송자의 인증 정보 확인을 위해
 * Spring Security의 {@code Authentication} 객체를 활용합니다.
 *
 * @author 유재원
 * @see com.eof.back.domain.gamesession.service.GamePlayService
 * @since 2026-03-26
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class GamePlayController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GamePlayService gamePlayService;
    private final GameSessionRepository gameSessionRepository;

    @MessageMapping("/rooms/{gameSessionId}/chat")
    public void chat(
            @DestinationVariable Long gameSessionId,
            @Payload ChatMessageRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL, "인증되지 않은 사용자의 채팅 시도입니다.");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        log.info("[방 {} 채팅] {} : {}", gameSessionId, userPrincipal.username(), request.message());

        GameMessageResponse<Void> response = GameMessageResponse.chat(
                userPrincipal.username(),
                request.message()
        );

        messagingTemplate.convertAndSend("/topic/rooms/" + gameSessionId + "/chat", response);
    }

    @MessageMapping("/rooms/{gameSessionId}/start")
    public void startGame(
            @DestinationVariable Long gameSessionId,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL, "인증되지 않은 사용자의 게임 시작 시도입니다.");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        log.info("방장 {} 님이 방 {} 의 게임을 시작했습니다.", userPrincipal.username(), gameSessionId);
        gamePlayService.startGame(gameSessionId);
    }

    @MessageMapping("/rooms/{gameSessionId}/answer")
    public void submitAnswer(
            @DestinationVariable Long gameSessionId,
            @Payload QuizAnswerRequest request,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        gamePlayService.submitAnswer(gameSessionId, userPrincipal.username(), request.answer());
    }
}