package com.eof.back.domain.auth.oauth2;

import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.token.TokenVersionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * OAuth2SuccessHandler의 단위 테스트입니다.
 * 소셜 로그인 성공 시 계정 상태 검증, JWT 발급, 쿠키 설정, redirect를 검증합니다.
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class OAuth2SuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private TokenVersionStore tokenVersionStore;

    @Mock
    private CookieUtil cookieUtil;

    @InjectMocks
    private OAuth2SuccessHandler successHandler;

    private static final String REDIRECT_URI = "http://localhost:3000/oauth/callback";
    private static final String ACCESS_TOKEN = "access.token";
    private static final String REFRESH_TOKEN = "refresh.token";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(successHandler, "redirectUri", REDIRECT_URI);
        ReflectionTestUtils.setField(successHandler, "refreshTokenExpireSeconds", 1209600L);
    }

    private OAuth2AuthenticationToken buildAuthToken(boolean isActive) {
        CustomOAuth2User customUser = new CustomOAuth2User(
                mock(OAuth2User.class), USER_ID, "GOOGLE_google_123", Role.USER, "홍길동", isActive
        );
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        given(authToken.getPrincipal()).willReturn(customUser);
        return authToken;
    }

    @Nested
    @DisplayName("onAuthenticationSuccess")
    class OnAuthenticationSuccess {

        @Test
        @DisplayName("성공 - JWT 쿠키를 설정하고 프론트엔드로 redirect한다")
        void success() throws Exception {
            given(tokenVersionStore.increment(USER_ID)).willReturn(1L);
            given(jwtTokenProvider.createAccessToken(eq(USER_ID), any(), any(), any(), eq(1L)))
                    .willReturn(ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(eq(USER_ID), any(), any(), any(), eq(1L))).willReturn(REFRESH_TOKEN);

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            successHandler.onAuthenticationSuccess(request, response, buildAuthToken(true));

            verify(cookieUtil).addAllTokenCookies(response, ACCESS_TOKEN, REFRESH_TOKEN);
            verify(refreshTokenStore).save(eq(USER_ID), eq(REFRESH_TOKEN), any());
            String expectedUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                    .queryParam("userId", USER_ID)
                    .queryParam("nickname", "홍길동")
                    .build()
                    .encode()
                    .toUriString();
            assertThat(response.getRedirectedUrl()).isEqualTo(expectedUrl);
        }

    }
}
