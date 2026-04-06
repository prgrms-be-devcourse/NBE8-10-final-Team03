package com.eof.back.global.websocket;

import com.eof.back.domain.gamesession.service.GameSessionService;
import com.eof.back.global.jwt.UserPrincipal;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 클라이언트의 연결 종료 이벤트를 감지하고 방 퇴장을 처리하는 리스너입니다.
 * <p>
 * STOMP 세션이 예기치 않게 끊어지거나 종료될 때 발생하는 {@code SessionDisconnectEvent}를 수신합니다.
 * 이벤트 발생 시 STOMP 헤더의 세션 속성에서 유저 인증 정보와 현재 참여 중인 게임 방 ID를 추출하며,
 * 정보가 유효할 경우 실시간 게임 세션에서 해당 유저를 안전하게 퇴장 처리하는 역할을 수행합니다.
 *
 *
 * <p><b>주요 생성자:</b><br>
 * {@code WebSocketEventListener(GameSessionService gameSessionService)} <br>
 * Lombok의 {@code @RequiredArgsConstructor}를 통해 유저의 방 퇴장 로직을 수행할 게임 세션 관리 서비스를 의존성 주입받습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Component} 어노테이션이 적용되어 Spring Application Context의 싱글톤 빈으로 자동 등록 및 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring WebSocket (STOMP 메시징 프로토콜 처리) 및 Spring Security (사용자 인증 객체 활용) 모듈에 의존합니다.
 *
 * @author 유재원
 * @see org.springframework.web.socket.messaging.SessionDisconnectEvent
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final GameSessionService gameSessionService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 1. 세션에서 유저 정보와 방 정보 꺼내기
        Authentication authentication = (Authentication) headerAccessor.getSessionAttributes().get("USER_AUTH");
        Long gameSessionId = (Long) headerAccessor.getSessionAttributes().get("CURRENT_ROOM_ID");

        // 2. 유저와 방 정보가 모두 있는 경우에만 퇴장 로직 실행
        if (authentication != null && gameSessionId != null) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.id();

            log.info("비정상 연결 종료 감지. userId: {}, gameSessionId: {}", userId, gameSessionId);

            try {
                gameSessionService.leaveRoom(userId, gameSessionId);
            } catch (Exception e) {

                log.info("Disconnect 퇴장 처리 중 무시 가능한 예외 발생: {}", e.getMessage());
            }
        }
    }
}