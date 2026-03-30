package com.eof.back.domain.auth.oauth2;

import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 소셜 로그인 성공 시 JWT 토큰을 발급하고 프론트엔드로 redirect하는 핸들러입니다.
 *
 * <p>Spring Security의 {@link SimpleUrlAuthenticationSuccessHandler}를 상속합니다.
 * OAuth2 인증이 완료되면 이 핸들러가 호출되어 다음을 수행합니다.</p>
 *
 * <p><b>처리 흐름:</b><br>
 * 1. 인증된 소셜 유저 정보로 DB에서 User 조회<br>
 * 2. AccessToken, RefreshToken 발급<br>
 * 3. RefreshToken을 저장소에 저장<br>
 * 4. 토큰을 HttpOnly 쿠키로 설정<br>
 * 5. 프론트엔드 redirect URI로 이동
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final CookieUtil cookieUtil;

    @Value("${custom.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${custom.jwt.refreshTokenExpirationSeconds}")
    private long refreshTokenExpireSeconds;

    /**
     * 소셜 로그인 성공 시 호출됩니다.
     *
     * <p>인증된 소셜 유저의 providerId로 DB에서 User를 조회한 뒤
     * JWT 토큰을 발급하고 쿠키에 담아 프론트엔드로 redirect합니다.
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
        OAuth2User oAuth2User = authToken.getPrincipal();
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        // 1. 소셜 제공자 응답을 파싱하여 providerId 추출
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());

        // 2. providerId로 DB에서 User 조회
        // CustomOAuth2UserService에서 이미 findOrCreateUser()가 실행됐으므로
        // 여기서 유저가 없으면 로직 오류 또는 데이터 정합성 문제 → OAuth2 인증 실패로 처리
        User user = userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("user_not_found"), "소셜 로그인 처리 중 오류가 발생했습니다."));

        // 3. 계정 상태 검증
        if (!user.isActive()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("login_fail"), "로그인에 실패하였습니다.");
        }

        // 4. AccessToken, RefreshToken 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getUsername(), user.getRole().name(), user.getNickname());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 5. RefreshToken 저장소에 저장
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds);
        refreshTokenStore.save(user.getId(), refreshToken, refreshTokenExpiredAt);

        // 6. 토큰을 HttpOnly 쿠키로 설정
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        // 7. 프론트엔드로 redirect
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
