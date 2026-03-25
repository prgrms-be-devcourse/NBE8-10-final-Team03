package com.eof.back.global.websocket;


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

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 1. 최초 연결(CONNECT) 시: 토큰 검사 후 신분증을 만들어서 세션 주머니에 넣습니다.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String bearerToken = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
            if (bearerToken == null) bearerToken = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER.toLowerCase());

            String token = resolveToken(bearerToken);

            if (token != null) {
                try {
                    Claims claims = jwtTokenProvider.validateToken(token);
                    Long userId = jwtTokenProvider.getUserId(claims);
                    String username = jwtTokenProvider.getUsername(claims);
                    String role = jwtTokenProvider.getRole(claims);

                    UserPrincipal userPrincipal = new UserPrincipal(userId, username);
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
            }
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

    private String resolveToken(String bearer) {
        if (bearer != null && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}