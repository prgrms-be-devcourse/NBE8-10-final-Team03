package com.eof.back.global.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 웹소켓 최초 연결(Handshake) 시 HTTP 요청의 쿠키에서 인증 토큰을 추출하는 인터셉터입니다.
 * <p>
 * 클라이언트가 웹소켓 연결을 시도할 때 동작하며, HTTP 요청의 쿠키 목록을 탐색하여
 * 토큰을 찾습니다. 추출된 토큰은 웹소켓 세션 속성(attributes)에 저장되며,
 * 이후 STOMP 연결 시점 등에 다른 핸들러(예: StompHandler)에서 인증에 사용할 수 있도록 전달하는 역할을 합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link org.springframework.web.socket.server.HandshakeInterceptor} 인터페이스 구현
 *
 * @author 유재원
 * @see org.springframework.web.socket.server.HandshakeInterceptor
 * @since 2026-03-31
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest req = servletRequest.getServletRequest();
            Cookie[] cookies = req.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        // 쿠키에서 토큰을 꺼내 웹소켓 세션 속성에 저장
                        attributes.put("accessToken", cookie.getValue());
                        break;
                    }
                }
            }
        }
        return true; // 연결을 허용하고, 검증은 StompHandler에서 진행
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}