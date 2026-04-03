package com.eof.back.domain.auth.service;

import com.eof.back.domain.auth.dto.LoginRequest;
import com.eof.back.domain.auth.dto.LoginResult;
import com.eof.back.domain.auth.dto.SignupRequest;
import com.eof.back.domain.auth.dto.SignupResponse;
import com.eof.back.domain.auth.entity.RefreshToken;
import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.token.TokenVersionStore;
import io.jsonwebtoken.Claims;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * AuthServiceImpl의 단위 테스트입니다.
 * <p>
 * signup, login, reissue, logout, withdraw 메서드의 성공 및 실패 시나리오를 검증합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AuthServiceImplTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private TokenVersionStore tokenVersionStore;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String REFRESH_TOKEN = "valid.refresh.token";
    private static final String NEW_ACCESS_TOKEN = "new.access.token";
    private static final String NEW_REFRESH_TOKEN = "new.refresh.token";
    private static final Long USER_ID = 1L;
    private static final long TOKEN_VERSION = 1L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpireSeconds", 604800L);
    }

    @Nested
    @DisplayName("signup")
    class Signup {

        @Test
        @DisplayName("성공 - 사용자를 생성하고 기본 정보를 반환한다")
        void success() {
            // given
            SignupRequest req = new SignupRequest("testUser", "password1", "tester");
            User savedUser = mock(User.class);

            given(userRepository.existsByUsername("testUser")).willReturn(false);
            given(userRepository.existsByNickname("tester")).willReturn(false);
            given(passwordEncoder.encode("password1")).willReturn("encodedPassword");
            given(userRepository.saveAndFlush(any(User.class))).willReturn(savedUser);
            given(savedUser.getId()).willReturn(1L);
            given(savedUser.getUsername()).willReturn("testUser");
            given(savedUser.getNickname()).willReturn("tester");

            // when
            SignupResponse response = authService.signup(req);

            // then
            assertThat(response.username()).isEqualTo("testUser");
            assertThat(response.nickname()).isEqualTo("tester");
        }

        @Test
        @DisplayName("실패 - 아이디 중복이면 USER_ALREADY_EXIST 예외가 발생한다")
        void fail_duplicateUsername() {
            // given
            SignupRequest req = new SignupRequest("testUser", "password1", "tester");
            given(userRepository.existsByUsername("testUser")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_ALREADY_EXIST));

            verify(userRepository, never()).saveAndFlush(any(User.class));
        }

        @Test
        @DisplayName("실패 - 닉네임 중복이면 NICKNAME_ALREADY_EXIST 예외가 발생한다")
        void fail_duplicateNickname() {
            // given
            SignupRequest req = new SignupRequest("testUser", "password1", "tester");
            given(userRepository.existsByUsername("testUser")).willReturn(false);
            given(userRepository.existsByNickname("tester")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.NICKNAME_ALREADY_EXIST));

            verify(userRepository, never()).saveAndFlush(any(User.class));
        }

        @Test
        @DisplayName("실패 - 동시 요청 충돌 시 SIGNUP_FAIL 예외가 발생한다")
        void fail_concurrentConflict() {
            // given
            SignupRequest req = new SignupRequest("testUser", "password1", "tester");
            given(userRepository.existsByUsername("testUser")).willReturn(false);
            given(userRepository.existsByNickname("tester")).willReturn(false);
            given(passwordEncoder.encode("password1")).willReturn("encodedPassword");
            given(userRepository.saveAndFlush(any(User.class)))
                    .willThrow(new DataIntegrityViolationException("unique constraint violation"));

            // when & then
            assertThatThrownBy(() -> authService.signup(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.SIGNUP_FAIL));
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("성공 - tokenVersion을 증가시키고 AccessToken, RefreshToken을 반환한다")
        void success() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123", null);
            User user = mock(User.class);

            given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
            given(user.getPassword()).willReturn("encodedPassword");
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(user.isActive()).willReturn(true);
            given(user.getId()).willReturn(USER_ID);
            given(user.getUsername()).willReturn("testUser");
            given(user.getRole()).willReturn(Role.USER);
            given(user.getNickname()).willReturn("tester");
            given(tokenVersionStore.increment(USER_ID)).willReturn(TOKEN_VERSION);
            given(jwtTokenProvider.createAccessToken(USER_ID, "testUser", "USER", "tester", TOKEN_VERSION))
                    .willReturn(NEW_ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(USER_ID, "testUser", "USER", "tester", TOKEN_VERSION))
                    .willReturn(NEW_REFRESH_TOKEN);

            // when
            LoginResult response = authService.login(req);

            // then
            assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
            assertThat(response.nickname()).isEqualTo("tester");
            verify(tokenVersionStore).increment(USER_ID);
            verify(refreshTokenStore).save(eq(USER_ID), eq(NEW_REFRESH_TOKEN), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("성공 - 재로그인 시 tokenVersion이 증가하여 이전 세션 토큰을 무효화한다")
        void success_relogin_incrementsVersion() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123", null);
            User user = mock(User.class);
            long newVersion = 5L; // 재로그인으로 version 증가

            given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
            given(user.getPassword()).willReturn("encodedPassword");
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(user.isActive()).willReturn(true);
            given(user.getId()).willReturn(USER_ID);
            given(user.getUsername()).willReturn("testUser");
            given(user.getRole()).willReturn(Role.USER);
            given(user.getNickname()).willReturn("tester");
            given(tokenVersionStore.increment(USER_ID)).willReturn(newVersion);
            given(jwtTokenProvider.createAccessToken(USER_ID, "testUser", "USER", "tester", newVersion))
                    .willReturn(NEW_ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(USER_ID, "testUser", "USER", "tester", newVersion))
                    .willReturn(NEW_REFRESH_TOKEN);

            // when
            authService.login(req);

            // then
            verify(tokenVersionStore).increment(USER_ID);
            verify(jwtTokenProvider).createAccessToken(USER_ID, "testUser", "USER", "tester", newVersion);
        }

        @Test
        @DisplayName("실패 - 로그인 시도 횟수 초과 시 LOGIN_ATTEMPTS_EXCEEDED 예외가 발생한다")
        void fail_loginAttemptsExceeded() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123", null);
            given(loginAttemptService.getAttemptCount("testUser")).willReturn(LoginAttemptService.MAX_ATTEMPTS);

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED));

            verify(userRepository, never()).findByUsername(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 아이디면 INVALID_CREDENTIALS 예외가 발생하고 실패 카운트를 기록하지 않는다")
        void fail_userNotFound() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123", null);

            given(userRepository.findByUsername("testUser")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS));

            verify(passwordEncoder, never()).matches(any(), any());
            verify(loginAttemptService, never()).recordFailure(any());
        }

        @Test
        @DisplayName("실패 - 비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 예외가 발생한다")
        void fail_invalidPassword() {
            // given
            LoginRequest req = new LoginRequest("testUser", "wrongPassword", null);
            User user = mock(User.class);

            given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
            given(user.getPassword()).willReturn("encodedPassword");
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS));

            verify(tokenVersionStore, never()).increment(any());
        }

        @Test
        @DisplayName("실패 - 탈퇴한 사용자면 LOGIN_FAIL 예외가 발생한다")
        void fail_deletedUser() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123", null);
            User user = mock(User.class);

            given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
            given(user.getPassword()).willReturn("encodedPassword");
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(user.isActive()).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.LOGIN_FAIL));

            verify(tokenVersionStore, never()).increment(any());
        }

        @Test
        @DisplayName("실패 - 정지된 사용자면 LOGIN_FAIL 예외가 발생한다")
        void fail_suspendedUser() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123", null);
            User user = mock(User.class);

            given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
            given(user.getPassword()).willReturn("encodedPassword");
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(user.isActive()).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.LOGIN_FAIL));

            verify(tokenVersionStore, never()).increment(any());
        }

        @Test
        @DisplayName("실패 - 실패 횟수가 임계값 이상이고 captchaToken이 없으면 CAPTCHA_REQUIRED 예외가 발생한다")
        void fail_captchaRequired() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123", null);

            given(loginAttemptService.getAttemptCount("testUser")).willReturn(CaptchaService.CAPTCHA_THRESHOLD);

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.CAPTCHA_REQUIRED));

            verify(userRepository, never()).findByUsername(any());
        }

        @Test
        @DisplayName("실패 - captchaToken이 있어도 검증 실패 시 CAPTCHA_INVALID 예외가 발생한다")
        void fail_captchaInvalid() {
            // given
            LoginRequest req = new LoginRequest("testUser", "password123", "invalid-token");

            given(loginAttemptService.getAttemptCount("testUser")).willReturn(CaptchaService.CAPTCHA_THRESHOLD);
            willThrow(new AuthException(AuthErrorCode.CAPTCHA_INVALID)).given(captchaService).verify("invalid-token");

            // when & then
            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.CAPTCHA_INVALID));

            verify(userRepository, never()).findByUsername(any());
            verify(loginAttemptService).recordFailure("testUser");
        }
    }

    @Nested
    @DisplayName("reissue")
    class Reissue {

        @Test
        @DisplayName("성공 - tokenVersion 일치 시 새로운 AccessToken과 RefreshToken을 반환한다")
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
            given(jwtTokenProvider.getUsername(claims)).willReturn("testuser");
            given(jwtTokenProvider.getRole(claims)).willReturn("USER");
            given(jwtTokenProvider.getNickname(claims)).willReturn("tester");
            given(jwtTokenProvider.getTokenVersion(claims)).willReturn(TOKEN_VERSION);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(savedToken));
            given(tokenVersionStore.findByUserId(USER_ID)).willReturn(Optional.of(TOKEN_VERSION));
            given(jwtTokenProvider.createAccessToken(USER_ID, "testuser", "USER", "tester", TOKEN_VERSION))
                    .willReturn(NEW_ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(USER_ID, "testuser", "USER", "tester", TOKEN_VERSION))
                    .willReturn(NEW_REFRESH_TOKEN);

            // when
            LoginResult response = authService.reissue(REFRESH_TOKEN);

            // then
            assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
            verify(refreshTokenStore).save(eq(USER_ID), eq(NEW_REFRESH_TOKEN), any(LocalDateTime.class));
            // reissue는 version을 증가시키지 않고 현재 값을 그대로 사용
            verify(tokenVersionStore, never()).increment(any());
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
        @DisplayName("실패 - DB에 저장된 RefreshToken이 만료되면 TOKEN_EXPIRED 예외가 발생한다")
        void fail_tokenExpiredInDb() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken expiredToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token(REFRESH_TOKEN)
                    .expiredAt(LocalDateTime.now().minusDays(1))
                    .build();

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(expiredToken));

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_EXPIRED));
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
        @DisplayName("실패 - Redis에 tokenVersion 키 없음(로그아웃 상태)이면 TOKEN_INVALID 예외가 발생한다")
        void fail_tokenVersionKeyAbsent() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken savedToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token(REFRESH_TOKEN)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(jwtTokenProvider.getUsername(claims)).willReturn("testuser");
            given(jwtTokenProvider.getRole(claims)).willReturn("USER");
            given(jwtTokenProvider.getNickname(claims)).willReturn("tester");
            given(jwtTokenProvider.getTokenVersion(claims)).willReturn(TOKEN_VERSION);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(savedToken));
            given(tokenVersionStore.findByUserId(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_INVALID));
        }

        @Test
        @DisplayName("실패 - tokenVersion 불일치(다른 기기 재로그인)이면 TOKEN_INVALID 예외가 발생한다")
        void fail_tokenVersionMismatch() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken savedToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token(REFRESH_TOKEN)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(jwtTokenProvider.getUsername(claims)).willReturn("testuser");
            given(jwtTokenProvider.getRole(claims)).willReturn("USER");
            given(jwtTokenProvider.getNickname(claims)).willReturn("tester");
            given(jwtTokenProvider.getTokenVersion(claims)).willReturn(1L); // refresh token의 version = 1
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(savedToken));
            given(tokenVersionStore.findByUserId(USER_ID)).willReturn(Optional.of(2L)); // Redis = 2 (재로그인)

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_INVALID));
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("성공 - RefreshToken과 tokenVersion을 저장소에서 삭제한다")
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
            verify(tokenVersionStore).delete(USER_ID);
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
        @DisplayName("실패 - DB에 저장된 RefreshToken이 만료되면 TOKEN_EXPIRED 예외가 발생한다")
        void fail_tokenExpiredInDb() {
            // given
            Claims claims = mock(Claims.class);
            RefreshToken expiredToken = RefreshToken.builder()
                    .userId(USER_ID)
                    .token(REFRESH_TOKEN)
                    .expiredAt(LocalDateTime.now().minusDays(1))
                    .build();

            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(claims);
            given(jwtTokenProvider.getUserId(claims)).willReturn(USER_ID);
            given(refreshTokenStore.findByUserId(USER_ID)).willReturn(Optional.of(expiredToken));

            // when & then
            assertThatThrownBy(() -> authService.logout(REFRESH_TOKEN))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.TOKEN_EXPIRED));
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

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("성공 - 사용자를 soft delete하고 RefreshToken과 tokenVersion을 삭제한다")
        void success() {
            // given
            User user = mock(User.class);

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(user.isDeleted()).willReturn(false);

            // when
            authService.withdraw(USER_ID);

            // then
            verify(user).delete();
            verify(refreshTokenStore).delete(USER_ID);
            verify(tokenVersionStore).delete(USER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
        void fail_userNotFound() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.withdraw(USER_ID))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 이미 탈퇴한 사용자면 USER_ALREADY_DELETED 예외가 발생한다")
        void fail_alreadyDeleted() {
            // given
            User user = mock(User.class);

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(user.isDeleted()).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.withdraw(USER_ID))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_ALREADY_DELETED));
        }
    }
}
