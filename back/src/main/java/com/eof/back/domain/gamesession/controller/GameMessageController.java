package com.eof.back.domain.gamesession.controller;

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
 * @since 2026-03-24
 */

import com.eof.back.domain.gamesession.dto.ChatMessageRequest;
import com.eof.back.domain.gamesession.dto.GameMessageResponse;
import com.eof.back.domain.user.dto.UserPrincipal;
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
            log.error(" 미인증 사용자의 채팅 시도입니다.");
            return;
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