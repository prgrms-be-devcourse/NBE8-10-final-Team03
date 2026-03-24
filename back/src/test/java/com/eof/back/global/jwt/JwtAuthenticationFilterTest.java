package com.eof.back.global.jwt;

import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 SecurityContext에 인증 정보가 저장된다")
    void validToken_setsAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = mock(Claims.class);
        when(jwtTokenProvider.validateToken("validToken")).thenReturn(claims);
        when(jwtTokenProvider.getUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.getUsername(claims)).thenReturn("testUser");
        when(jwtTokenProvider.getRole(claims)).thenReturn("USER");

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
    @DisplayName("Authorization 헤더가 없으면 SecurityContext에 인증 정보가 저장되지 않는다")
    void noToken_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtTokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 접두어가 없는 헤더는 무시된다")
    void tokenWithoutBearerPrefix_ignored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic someToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtTokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("만료된 토큰이면 SecurityContext가 비워지고 다음 필터로 넘어간다")
    void expiredToken_clearsContextAndContinues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expiredToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

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
        request.addHeader("Authorization", "Bearer invalidToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doThrow(new AuthException(AuthErrorCode.TOKEN_INVALID))
                .when(jwtTokenProvider).validateToken("invalidToken");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
