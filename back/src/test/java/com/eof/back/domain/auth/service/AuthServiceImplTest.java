package com.eof.back.domain.auth.service;

import com.eof.back.domain.auth.dto.LoginRequest;
import com.eof.back.domain.auth.dto.LoginResponse;
import com.eof.back.domain.auth.dto.ReissueResponse;
import com.eof.back.domain.auth.entity.RefreshToken;
import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.domain.user.entity.Role;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * AuthServiceImpl의 단위 테스트입니다.
 * <p>
 * reissue, logout 두 메서드의 성공 및 실패 시나리오를 검증합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String REFRESH_TOKEN = "valid.refresh.token";
    private static final String NEW_ACCESS_TOKEN = "new.access.token";
    private static final String NEW_REFRESH_TOKEN = "new.refresh.token";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpireSeconds", 604800L);
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("성공 - AccessToken과 RefreshToken을 반환한다")
        void success() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123");
            User user = mock(User.class);

            given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
            given(user.getPassword()).willReturn("encodedPassword");
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(user.getId()).willReturn(USER_ID);
            given(user.getUsername()).willReturn("testUser");
            given(user.getRole()).willReturn(Role.USER);
            given(user.getNickname()).willReturn("tester");
            given(jwtTokenProvider.createAccessToken(USER_ID, "testUser", "USER")).willReturn(NEW_ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(USER_ID)).willReturn(NEW_REFRESH_TOKEN);

            // when
            LoginResponse response = authService.login(req);

            // then
            assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
            assertThat(response.nickname()).isEqualTo("tester");
            verify(refreshTokenStore).save(eq(USER_ID), eq(NEW_REFRESH_TOKEN), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 아이디면 INVALID_CREDENTIALS 예외가 발생한다")
        void fail_userNotFound() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123");
            given(userRepository.findByUsername("testUser")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS));

            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("실패 - 비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 예외가 발생한다")
        void fail_invalidPassword() {
            // given
            LoginRequest req = new LoginRequest("testUser", "wrongPassword");
            User user = mock(User.class);

            given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
            given(user.getPassword()).willReturn("encodedPassword");
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS));

            verify(jwtTokenProvider, never()).createAccessToken(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("reissue")
    class Reissue {

        @Test
        @DisplayName("성공 - 새로운 AccessToken과 RefreshToken을 반환한다")
        void success() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken savedToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token(REFRESH_TOKEN)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();
            User user = mock(User.class);

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(savedToken));
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(user.getId()).willReturn(USER_ID);
            given(user.getUsername()).willReturn("testuser");
            given(user.getRole()).willReturn(Role.USER);
            given(jwtTokenProvider.createAccessToken(USER_ID, "testuser", "USER")).willReturn(NEW_ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(USER_ID)).willReturn(NEW_REFRESH_TOKEN);

            // when
            ReissueResponse response = authService.reissue(REFRESH_TOKEN);

            // then
            assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
            verify(refreshTokenStore).save(eq(USER_ID), eq(NEW_REFRESH_TOKEN), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("실패 - JWT 토큰이 유효하지 않으면 TOKEN_INVALID 예외가 발생한다")
        void fail_invalidToken() {
            // given
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN))
                    .willThrow(new AuthException(AuthErrorCode.TOKEN_INVALID));

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_INVALID));
        }

        @Test
        @DisplayName("실패 - JWT 토큰이 만료되면 TOKEN_EXPIRED 예외가 발생한다")
        void fail_expiredToken() {
            // given
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN))
                    .willThrow(new AuthException(AuthErrorCode.TOKEN_EXPIRED));

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_EXPIRED));
        }

        @Test
        @DisplayName("실패 - 저장된 RefreshToken이 없으면 TOKEN_INVALID 예외가 발생한다")
        void fail_tokenNotFound() {
            // given
            Claims claims = mock(Claims.class);
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_INVALID));
        }

        @Test
        @DisplayName("실패 - 저장된 토큰과 요청 토큰이 다르면 TOKEN_INVALID 예외가 발생한다")
        void fail_tokenMismatch() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken savedToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token("different.token")
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(savedToken));

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_INVALID));
        }

        @Test
        @DisplayName("실패 - 사용자가 존재하지 않으면 INVALID_CREDENTIALS 예외가 발생한다")
        void fail_userNotFound() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken savedToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token(REFRESH_TOKEN)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(savedToken));
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS));
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("성공 - RefreshToken을 저장소에서 삭제한다")
        void success() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken savedToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token(REFRESH_TOKEN)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(savedToken));

            // when
            authService.logout(REFRESH_TOKEN);

            // then
            verify(refreshTokenStore).delete(USER_ID);
        }

        @Test
        @DisplayName("실패 - JWT 토큰이 유효하지 않으면 TOKEN_INVALID 예외가 발생한다")
        void fail_invalidToken() {
            // given
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN))
                    .willThrow(new AuthException(AuthErrorCode.TOKEN_INVALID));

            // when & then
            assertThatThrownBy(() -> authService.logout(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_INVALID));
        }

        @Test
        @DisplayName("실패 - 저장된 RefreshToken이 없으면 TOKEN_INVALID 예외가 발생한다")
        void fail_tokenNotFound() {
            // given
            Claims claims = mock(Claims.class);
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.logout(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_INVALID));
        }

        @Test
        @DisplayName("실패 - 저장된 토큰과 요청 토큰이 다르면 TOKEN_INVALID 예외가 발생한다")
        void fail_tokenMismatch() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken savedToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token("different.token")
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(savedToken));

            // when & then
            assertThatThrownBy(() -> authService.logout(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_INVALID));
        }
    }
}
