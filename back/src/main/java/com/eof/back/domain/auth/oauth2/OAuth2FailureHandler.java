package com.eof.back.domain.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 소셜 로그인 실패 시 프론트엔드로 redirect하는 핸들러입니다.
 *
 * <p>Spring Security의 {@link SimpleUrlAuthenticationFailureHandler}를 상속합니다.
 * OAuth2 인증 실패 시 이 핸들러가 호출되며, 에러 메시지를 쿼리 파라미터로 담아
 * 프론트엔드로 redirect합니다.</p>
 *
 * <p>프론트엔드는 {@code error} 파라미터를 읽어 사용자에게 에러를 표시해야 합니다.<br>
 * 예: {@code http://localhost:3000/oauth/callback?error=소셜+로그인에+실패하였습니다.}
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${custom.oauth2.redirect-uri}")
    private String redirectUri;

    /**
     * 소셜 로그인 실패 시 호출됩니다.
     * 에러 메시지를 URL 인코딩하여 프론트엔드로 redirect합니다.
     *
     * @param request   HTTP 요청
     * @param response  HTTP 응답
     * @param exception 인증 실패 예외
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorMessage = URLEncoder.encode("소셜 로그인에 실패하였습니다.", StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?error=" + errorMessage);
    }
}
