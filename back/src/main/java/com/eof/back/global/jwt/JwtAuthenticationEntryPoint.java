package com.eof.back.global.jwt;

import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.response.CommonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 요청에 대한 진입점 처리 클래스입니다.
 *
 * <p>Authorization 헤더 없이 보호된 리소스에 접근하면 호출되며,
 * 프로젝트의 공통 응답 형식으로 401을 반환합니다.
 *
 * @author 5h6vm
 * @since 2026-03-19
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        CommonResponse.fail(AuthErrorCode.LOGIN_REQUIRED.getMessage())
                )
        );
    }
}
