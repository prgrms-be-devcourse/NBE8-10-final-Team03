package com.eof.back.domain.auth.oauth2;

import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.token.TokenVersionStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 소셜 로그인 성공 시 JWT 토큰을 발급하고 프론트엔드로 redirect하는 핸들러입니다.
 *
 * <p>Spring Security의 {@link SimpleUrlAuthenticationSuccessHandler}를 상속합니다.
 * {@link CustomOAuth2UserService}가 {@link CustomOAuth2User}에 User 정보를 담아 전달하므로,
 * 이 핸들러에서는 DB 재조회나 OAuthAttributes 재파싱 없이 바로 JWT를 발급할 수 있습니다.</p>
 *
 * <p><b>처리 흐름:</b><br>
 * 1. CustomOAuth2User에서 사용자 정보 추출<br>
 * 2. 계정 활성 상태 검증<br>
 * 3. AccessToken, RefreshToken 발급<br>
 * 4. RefreshToken을 저장소에 저장<br>
 * 5. 토큰을 HttpOnly 쿠키로 설정<br>
 * 6. 프론트엔드 redirect URI로 이동
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenVersionStore tokenVersionStore;
    private final CookieUtil cookieUtil;

    @Value("${custom.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${custom.jwt.refreshTokenExpirationSeconds}")
    private long refreshTokenExpireSeconds;

    /**
     * 소셜 로그인 성공 시 호출됩니다.
     *
     * <p>CustomOAuth2User에서 사용자 정보를 꺼내 JWT 토큰을 발급하고
     * 쿠키에 담아 프론트엔드로 redirect합니다.
     *
     * @param request        HTTP 요청
     * @param response       HTTP 응답 (쿠키 설정에 사용)
     * @param authentication 인증 완료된 OAuth2 인증 객체
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        CustomOAuth2User customUser = (CustomOAuth2User) authToken.getPrincipal();

        // 1. AccessToken, RefreshToken 발급
        long tokenVersion = tokenVersionStore.increment(customUser.getUserId());
        String accessToken = jwtTokenProvider.createAccessToken(
                customUser.getUserId(), customUser.getUsername(), customUser.getRole().name(), customUser.getNickname(), tokenVersion);
        String refreshToken = jwtTokenProvider.createRefreshToken(
                customUser.getUserId(), customUser.getUsername(), customUser.getRole().name(), customUser.getNickname());

        // 2. RefreshToken 저장소에 저장
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds);
        refreshTokenStore.save(customUser.getUserId(), refreshToken, refreshTokenExpiredAt);

        // 3. 토큰을 HttpOnly 쿠키로 설정
        cookieUtil.addAllTokenCookies(response, accessToken, refreshToken);

        // 4. 프론트엔드로 redirect
        String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("userId", customUser.getUserId())
                .queryParam("nickname", customUser.getNickname())
                .build()
                .encode()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
