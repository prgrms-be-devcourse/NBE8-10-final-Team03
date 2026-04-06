package com.eof.back.global.websocket;


import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 웹소켓(STOMP) 연결 및 메시지 전송 시 JWT 기반의 사용자 인증을 처리하는 채널 인터셉터입니다.
 * <p>
 * 클라이언트가 웹소켓에 최초 연결(CONNECT)을 시도할 때 헤더에서 JWT를 추출하여 유효성을 검증합니다.
 * 검증에 성공하면 사용자 정보(UserPrincipal)를 담은 인증(Authentication) 객체를 생성하고,
 * 이를 웹소켓 세션 속성에 보관합니다. 이후 연결이 끊어지기 전까지 발생하는 모든 메시지 요청에 대해
 * 세션에서 인증 정보를 꺼내어 연속적인 사용자 식별 상태를 유지합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link org.springframework.messaging.support.ChannelInterceptor} 인터페이스 구현
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Component} 어노테이션을 통해 스프링 빈으로 관리되며,
 * WebSocketConfig 등에서 MessageChannel의 인터셉터로 등록되어 사용됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 토큰 추출 및 검증 처리를 위해 {@code JwtTokenProvider}에 의존합니다.
 *
 * @author 유재원
 * @see org.springframework.messaging.support.ChannelInterceptor
 * @since 2026-03-25
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final GameSessionRepository gameSessionRepository;
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 1. 최초 연결(CONNECT) 시: 토큰 검사 후 신분증을 만들어서 세션 주머니에 넣습니다.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = (String) accessor.getSessionAttributes().get("accessToken");

            // 토큰 누락 시 예외 발생
            if (token == null || token.isBlank()) {
                log.error("웹소켓 연결 거부: 쿠키에서 인증 토큰을 찾을 수 없습니다.");
                throw new AuthException(AuthErrorCode.TOKEN_INVALID, "인증 토큰이 존재하지 않습니다.");
            }
            try {
                Claims claims = jwtTokenProvider.validateToken(token);
                Long userId = jwtTokenProvider.getUserId(claims);
                String username = jwtTokenProvider.getUsername(claims);
                String role = jwtTokenProvider.getRole(claims);
                String nickname = jwtTokenProvider.getNickname(claims);

                UserPrincipal userPrincipal = new UserPrincipal(userId, username, nickname, role);
                String authority = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, List.of(new SimpleGrantedAuthority(authority))
                );

                //  세션에 신분증 영구 보관
                accessor.getSessionAttributes().put("USER_AUTH", authentication);
                accessor.setUser(authentication);

                log.info("웹소켓 연결 성공! 인증된 사용자: {}", username);

                accessor.setLeaveMutable(true);
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

            } catch (Exception e) {
                log.error(" 토큰 검증 오류: {}", e.getMessage());
                throw new AuthException(AuthErrorCode.TOKEN_INVALID, "유효하지 않은 JWT 토큰입니다.");
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // 2. 방 입장(SUBSCRIBE) 시: 어느 방에 들어갔는지 세션에 기록
            String destination = accessor.getDestination();

            // 구독하는 목적지가 채팅방일 경우 (ex) /topic/rooms/12/chat
            if (destination != null && destination.startsWith("/topic/rooms/")) {
                String[] parts = destination.split("/");
                if (parts.length >= 4) {
                    try {
                        Long gameSessionId = Long.valueOf(parts[3]);

                        Authentication authentication = (Authentication) accessor.getSessionAttributes().get("USER_AUTH");
                        if (authentication == null) {
                            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL, "인증되지 않은 사용자입니다.");
                        }
                        Long userId = ((UserPrincipal) authentication.getPrincipal()).id();

                        // 실제 방 참가자인지 DB 검증
                        GameSession gameSession = gameSessionRepository.findByIdWithPlayers(gameSessionId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

                        boolean isParticipant = gameSession.getHost().getId().equals(userId) ||
                                gameSession.getPlayers().stream().anyMatch(p -> p.getId().equals(userId));

                        if (!isParticipant) {
                            log.warn("권한 없는 방 구독 시도 차단 userId: {}, roomId: {}", userId, gameSessionId);
                            // STOMP 구독 거절
                            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL, "방에 참여한 유저만 통신에 연결할 수 있습니다.");
                        }

                        accessor.getSessionAttributes().put("CURRENT_ROOM_ID", gameSessionId);

                    } catch (NumberFormatException e) {
                        log.warn("잘못된 형식의 방 번호 구독 요청: {}", destination);
                    }
                }
            }

            Authentication authentication = (Authentication) accessor.getSessionAttributes().get("USER_AUTH");
            if (authentication != null) {
                accessor.setUser(authentication);
                accessor.setLeaveMutable(true);
            }
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

        } else if (accessor.getCommand() != null && !StompCommand.DISCONNECT.equals(accessor.getCommand())) {

            Authentication authentication = (Authentication) accessor.getSessionAttributes().get("USER_AUTH");

            if (authentication != null) {
                accessor.setUser(authentication);
                accessor.setLeaveMutable(true);
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            }
        }

        return message;
    }
}