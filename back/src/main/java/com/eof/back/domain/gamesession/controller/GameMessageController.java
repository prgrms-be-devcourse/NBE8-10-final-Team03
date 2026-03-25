package com.eof.back.domain.gamesession.controller;

import com.eof.back.domain.gamesession.dto.ChatMessageRequest;
import com.eof.back.domain.gamesession.dto.GameMessageResponse;
import com.eof.back.domain.user.dto.UserPrincipal;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * 웹소켓(STOMP)을 통해 클라이언트로부터 들어오는 실시간 메시지를 처리하는 컨트롤러입니다.
 *
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class GameMessageController {

    private final SimpMessagingTemplate messagingTemplate;

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
}