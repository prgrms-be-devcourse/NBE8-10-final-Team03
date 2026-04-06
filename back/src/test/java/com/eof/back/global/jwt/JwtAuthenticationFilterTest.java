package com.eof.back.global.jwt;

import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.token.TokenVersionStore;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenVersionStore tokenVersionStore;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, tokenVersionStore, cookieUtil);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 accessToken + tokenVersion 일치 시 SecurityContext에 인증 정보가 저장된다")
    void validToken_matchingVersion_setsAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieUtil.ACCESS_TOKEN_COOKIE, "validToken"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mock(Claims.class);
        when(cookieUtil.resolveToken(request, CookieUtil.ACCESS_TOKEN_COOKIE))
                .thenReturn(Optional.of("validToken"));
        when(jwtTokenProvider.validateToken("validToken")).thenReturn(claims);
        when(jwtTokenProvider.getUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.getUsername(claims)).thenReturn("testUser");
        when(jwtTokenProvider.getRole(claims)).thenReturn("USER");
        when(jwtTokenProvider.getNickname(claims)).thenReturn("tester");
        when(jwtTokenProvider.getTokenVersion(claims)).thenReturn(3L);
        when(tokenVersionStore.findByUserId(1L)).thenReturn(Optional.of(3L));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isInstanceOf(UserPrincipal.class)
                .satisfies(p -> {
                    UserPrincipal principal = (UserPrincipal) p;
                    assertThat(principal.id()).isEqualTo(1L);
                    assertThat(principal.username()).isEqualTo("testUser");
                });
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("accessToken 쿠키가 없으면 SecurityContext에 인증 정보가 저장되지 않는다")
    void noToken_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(cookieUtil.resolveToken(request, CookieUtil.ACCESS_TOKEN_COOKIE))
                .thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtTokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("만료된 토큰이면 SecurityContext가 비워지고 다음 필터로 넘어간다")
    void expiredToken_clearsContextAndContinues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieUtil.ACCESS_TOKEN_COOKIE, "expiredToken"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(cookieUtil.resolveToken(request, CookieUtil.ACCESS_TOKEN_COOKIE))
                .thenReturn(Optional.of("expiredToken"));
        doThrow(new AuthException(AuthErrorCode.TOKEN_EXPIRED))
                .when(jwtTokenProvider).validateToken("expiredToken");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 SecurityContext가 비워지고 다음 필터로 넘어간다")
    void invalidToken_clearsContextAndContinues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieUtil.ACCESS_TOKEN_COOKIE, "invalidToken"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(cookieUtil.resolveToken(request, CookieUtil.ACCESS_TOKEN_COOKIE))
                .thenReturn(Optional.of("invalidToken"));
        doThrow(new AuthException(AuthErrorCode.TOKEN_INVALID))
                .when(jwtTokenProvider).validateToken("invalidToken");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("tokenVersion 불일치 시 SecurityContext가 비워지고 다음 필터로 넘어간다 (다른 기기 재로그인)")
    void tokenVersionMismatch_clearsContextAndContinues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieUtil.ACCESS_TOKEN_COOKIE, "oldToken"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mock(Claims.class);
        when(cookieUtil.resolveToken(request, CookieUtil.ACCESS_TOKEN_COOKIE))
                .thenReturn(Optional.of("oldToken"));
        when(jwtTokenProvider.validateToken("oldToken")).thenReturn(claims);
        when(jwtTokenProvider.getUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.getUsername(claims)).thenReturn("testUser");
        when(jwtTokenProvider.getRole(claims)).thenReturn("USER");
        when(jwtTokenProvider.getNickname(claims)).thenReturn("tester");
        when(jwtTokenProvider.getTokenVersion(claims)).thenReturn(1L); // 토큰의 버전 = 1
        when(tokenVersionStore.findByUserId(1L)).thenReturn(Optional.of(2L)); // Redis의 버전 = 2 (재로그인)

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Redis에 tokenVersion 키 없음 시 SecurityContext가 비워진다 (로그아웃/탈퇴 상태)")
    void tokenVersionKeyAbsent_clearsContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieUtil.ACCESS_TOKEN_COOKIE, "token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mock(Claims.class);
        when(cookieUtil.resolveToken(request, CookieUtil.ACCESS_TOKEN_COOKIE))
                .thenReturn(Optional.of("token"));
        when(jwtTokenProvider.validateToken("token")).thenReturn(claims);
        when(jwtTokenProvider.getUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.getUsername(claims)).thenReturn("testUser");
        when(jwtTokenProvider.getRole(claims)).thenReturn("USER");
        when(jwtTokenProvider.getNickname(claims)).thenReturn("tester");
        when(jwtTokenProvider.getTokenVersion(claims)).thenReturn(1L);
        when(tokenVersionStore.findByUserId(1L)).thenReturn(Optional.empty()); // 키 없음

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
