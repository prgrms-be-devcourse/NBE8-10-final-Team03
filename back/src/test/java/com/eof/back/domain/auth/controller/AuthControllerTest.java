package com.eof.back.domain.auth.controller;

import com.eof.back.domain.auth.dto.LoginResult;
import com.eof.back.domain.auth.dto.SignupResponse;
import com.eof.back.domain.auth.service.AuthService;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptionHadler.DefaultExceptionHandler;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.jwt.UserPrincipal;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@Import({AuthControllerTest.MockSecurityConfig.class, DefaultExceptionHandler.class})
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Nested
    @DisplayName("signup")
    class Signup {

        @Test
        @DisplayName("성공 - 회원가입 후 201을 반환한다")
        void success() throws Exception {
            given(authService.signup(any()))
                    .willReturn(new SignupResponse(1L, "testuser", "테스트닉네임"));

            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"password1\",\"nickname\":\"테스트닉네임\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.nickname").value("테스트닉네임"))
                    .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));
        }

        @Test
        @DisplayName("실패 - 중복 아이디면 409를 반환한다")
        void fail_duplicateUsername() throws Exception {
            given(authService.signup(any()))
                    .willThrow(new AuthException(AuthErrorCode.USER_ALREADY_EXIST));

            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"password1\",\"nickname\":\"테스트닉네임\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("이미 존재하는 아이디입니다."));
        }

        @Test
        @DisplayName("실패 - 중복 닉네임이면 409를 반환한다")
        void fail_duplicateNickname() throws Exception {
            given(authService.signup(any()))
                    .willThrow(new AuthException(AuthErrorCode.NICKNAME_ALREADY_EXIST));

            mockMvc.perform(post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"password1\",\"nickname\":\"테스트닉네임\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."));
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("성공 - 로그인 후 userId, nickname을 반환하고 토큰은 쿠키로 설정된다")
        void success() throws Exception {
            given(authService.login(any()))
                    .willReturn(new LoginResult("access.token", "refresh.token", 1L, "테스트닉네임", "USER"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"password1\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(1L))
                    .andExpect(jsonPath("$.data.nickname").value("테스트닉네임"))
                    .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."));
        }

        @Test
        @DisplayName("실패 - 잘못된 자격증명이면 401을 반환한다")
        void fail_invalidCredentials() throws Exception {
            given(authService.login(any()))
                    .willThrow(new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"wrongpass\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."));
        }

        @Test
        @DisplayName("실패 - 탈퇴한 사용자면 410을 반환한다")
        void fail_deletedUser() throws Exception {
            given(authService.login(any()))
                    .willThrow(new AuthException(AuthErrorCode.USER_ALREADY_DELETED));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"password1\"}"))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.message").value("이미 탈퇴한 사용자입니다."));
        }

        @Test
        @DisplayName("실패 - 로그인 시도 횟수 초과 시 429를 반환한다")
        void fail_loginAttemptsExceeded() throws Exception {
            given(authService.login(any()))
                    .willThrow(new AuthException(AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"password1\"}"))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.message").value("로그인 시도 횟수를 초과하였습니다. 잠시 후 다시 시도해주세요."));
        }

        @Test
        @DisplayName("실패 - CAPTCHA 인증이 필요한 경우 403을 반환한다")
        void fail_captchaRequired() throws Exception {
            given(authService.login(any()))
                    .willThrow(new AuthException(AuthErrorCode.CAPTCHA_REQUIRED));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"password1\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("보안 문자 인증이 필요합니다."));
        }

        @Test
        @DisplayName("실패 - CAPTCHA 검증 실패 시 400을 반환한다")
        void fail_captchaInvalid() throws Exception {
            given(authService.login(any()))
                    .willThrow(new AuthException(AuthErrorCode.CAPTCHA_INVALID));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testuser\",\"password\":\"password1\",\"captchaToken\":\"bad-token\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("보안 문자 인증에 실패하였습니다."));
        }
    }

    @Nested
    @DisplayName("reissue")
    class Reissue {

        @Test
        @DisplayName("성공 - 쿠키의 refreshToken으로 재발급하고 새 토큰을 쿠키로 설정한다")
        void success() throws Exception {
            given(cookieUtil.resolveToken(any(), any()))
                    .willReturn(Optional.of("valid.refresh.token"));
            given(authService.reissue(any()))
                    .willReturn(new LoginResult("new.access.token", "new.refresh.token", 1L, "테스트닉네임", "USER"));

            mockMvc.perform(post("/api/v1/auth/reissue")
                            .cookie(new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE, "valid.refresh.token")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("토큰이 재발급되었습니다."));
        }

        @Test
        @DisplayName("실패 - refreshToken 쿠키가 없으면 401을 반환한다")
        void fail_noCookie() throws Exception {
            given(cookieUtil.resolveToken(any(), any()))
                    .willReturn(Optional.empty());

            mockMvc.perform(post("/api/v1/auth/reissue"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 토큰이면 401을 반환한다")
        void fail_invalidToken() throws Exception {
            given(cookieUtil.resolveToken(any(), any()))
                    .willReturn(Optional.of("invalid.token"));
            given(authService.reissue(any()))
                    .willThrow(new AuthException(AuthErrorCode.TOKEN_INVALID));

            mockMvc.perform(post("/api/v1/auth/reissue")
                            .cookie(new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE, "invalid.token")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("성공 - 로그아웃 후 200을 반환하고 쿠키가 삭제된다")
        void success() throws Exception {
            given(cookieUtil.resolveToken(any(), any()))
                    .willReturn(Optional.of("valid.refresh.token"));
            willDoNothing().given(authService).logout(any());

            mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE, "valid.refresh.token")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."));
        }

        @Test
        @DisplayName("실패 - refreshToken 쿠키가 없으면 401을 반환한다")
        void fail_noCookie() throws Exception {
            given(cookieUtil.resolveToken(any(), any()))
                    .willReturn(Optional.empty());

            mockMvc.perform(post("/api/v1/auth/logout"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 토큰이면 401을 반환한다")
        void fail_invalidToken() throws Exception {
            given(cookieUtil.resolveToken(any(), any()))
                    .willReturn(Optional.of("invalid.token"));
            willThrow(new AuthException(AuthErrorCode.TOKEN_INVALID))
                    .given(authService).logout(any());

            mockMvc.perform(post("/api/v1/auth/logout")
                            .cookie(new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE, "invalid.token")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
        }
    }

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("성공 - 회원탈퇴 후 204를 반환한다")
        void success() throws Exception {
            willDoNothing().given(authService).withdraw(1L);

            mockMvc.perform(delete("/api/v1/auth/withdraw"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 404를 반환한다")
        void fail_userNotFound() throws Exception {
            willThrow(new AuthException(AuthErrorCode.USER_NOT_FOUND))
                    .given(authService).withdraw(1L);

            mockMvc.perform(delete("/api/v1/auth/withdraw"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("실패 - 이미 탈퇴한 사용자면 410을 반환한다")
        void fail_alreadyDeleted() throws Exception {
            willThrow(new AuthException(AuthErrorCode.USER_ALREADY_DELETED))
                    .given(authService).withdraw(1L);

            mockMvc.perform(delete("/api/v1/auth/withdraw"))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.message").value("이미 탈퇴한 사용자입니다."));
        }
    }

    @TestConfiguration
    static class MockSecurityConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(UserPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {
                    return new UserPrincipal(1L, "testuser", "tester", "USER");
                }
            });
        }
    }
}
