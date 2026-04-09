package com.eof.back.domain.user.user.controller;

import com.eof.back.domain.auth.dto.LoginResult;
import com.eof.back.domain.auth.service.AuthService;
import com.eof.back.domain.user.user.dto.UserInfoResponse;
import com.eof.back.domain.user.user.dto.UserUpdateResponse;
import com.eof.back.domain.user.user.service.UserService;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptionHadler.DefaultExceptionHandler;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.token.TokenVersionStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@Import(DefaultExceptionHandler.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private TokenVersionStore tokenVersionStore;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Nested
    @DisplayName("getMyInfo")
    class GetMyInfo {

        @Test
        @DisplayName("성공 - 사용자 정보를 반환한다")
        void success() throws Exception {
            Long userId = 1L;

            given(userService.getInfo(userId))
                    .willReturn(new UserInfoResponse(userId, "testuser", "테스트닉네임", "USER",1));

            mockMvc.perform(get("/api/v1/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.nickname").value("테스트닉네임"))
                    .andExpect(jsonPath("$.message").value("내 정보 조회에 성공했습니다."));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 404를 반환한다")
        void fail_userNotFound() throws Exception {
            given(userService.getInfo(1L))
                    .willThrow(new AuthException(AuthErrorCode.USER_NOT_FOUND));

            mockMvc.perform(get("/api/v1/users/1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("updateMyInfo")
    class UpdateMyInfo {

        @Test
        @DisplayName("성공 - 닉네임 변경 시 토큰 재발급 후 쿠키에 설정한다")
        void success_withNickname_reissuesToken() throws Exception {
            Long userId = 1L;
            LoginResult loginResult = new LoginResult("newAccessToken", "newRefreshToken", userId, "새닉네임", "USER", null);

            given(userService.updateInfo(eq(userId), any()))
                    .willReturn(new UserUpdateResponse(userId, "새닉네임"));
            given(authService.reissueAfterNicknameChange(userId))
                    .willReturn(loginResult);

            mockMvc.perform(patch("/api/v1/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"새닉네임\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                    .andExpect(jsonPath("$.message").value("내 정보 수정이 완료되었습니다."));

            verify(authService).reissueAfterNicknameChange(userId);
            verify(cookieUtil).addAllTokenCookies(any(), eq("newAccessToken"), eq("newRefreshToken"));
        }

        @Test
        @DisplayName("성공 - 닉네임 없이 수정 시 토큰 재발급하지 않는다")
        void success_withoutNickname_noReissue() throws Exception {
            Long userId = 1L;

            given(userService.updateInfo(eq(userId), any()))
                    .willReturn(new UserUpdateResponse(userId, "기존닉네임"));

            mockMvc.perform(patch("/api/v1/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"currentPassword\":\"oldPass12\",\"password\":\"newPass12\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("내 정보 수정이 완료되었습니다."));

            verify(authService, never()).reissueAfterNicknameChange(any());
            verify(cookieUtil, never()).addAllTokenCookies(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 404를 반환한다")
        void fail_userNotFound() throws Exception {
            given(userService.updateInfo(eq(1L), any()))
                    .willThrow(new AuthException(AuthErrorCode.USER_NOT_FOUND));

            mockMvc.perform(patch("/api/v1/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"새닉네임\"}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."));
        }
    }
}
