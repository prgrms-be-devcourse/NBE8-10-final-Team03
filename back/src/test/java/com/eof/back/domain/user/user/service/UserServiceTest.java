package com.eof.back.domain.user.user.service;

import com.eof.back.domain.user.user.dto.UserInfoResponse;
import com.eof.back.domain.user.user.dto.UserUpdateRequest;
import com.eof.back.domain.user.user.dto.UserUpdateResponse;
import com.eof.back.domain.user.user.entity.AuthProvider;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService의 비즈니스 로직을 검증하는 단위 테스트 클래스입니다.
 *
 * <p>Mockito를 사용하여 의존 객체(UserRepository)를 Mock 처리하고
 * 서비스 계층의 로직이 의도한 대로 동작하는지 검증합니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("getInfo")
    class GetInfo {

        @Test
        @DisplayName("성공 - 사용자 정보를 반환한다")
        void success() {
            User user = User.of("testUser", "encodedPassword", "tester");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserInfoResponse result = userService.getInfo(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.username()).isEqualTo("testUser");
            assertThat(result.nickname()).isEqualTo("tester");
            assertThat(result.role()).isEqualTo("USER");
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
        void fail_userNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getInfo(1L))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("updateInfo")
    class UpdateInfo {

        @Test
        @DisplayName("성공 - 닉네임만 변경")
        void success_nicknameOnly() {
            User user = User.of("testUser", "encodedPassword", "oldNick");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
            when(userRepository.existsByNickname("newNick")).thenReturn(false);

            UserUpdateRequest request = new UserUpdateRequest("currentPass", "newNick", null,1);

            UserUpdateResponse result = userService.updateInfo(1L, request);

            assertThat(result.nickname()).isEqualTo("newNick");
            verify(userRepository).existsByNickname("newNick");
        }

        @Test
        @DisplayName("성공 - 비밀번호만 변경")
        void success_passwordOnly() {
            User user = User.of("testUser", "oldEncodedPassword", "tester");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("currentPass", "oldEncodedPassword")).thenReturn(true);
            when(passwordEncoder.matches("newPass1234", "oldEncodedPassword")).thenReturn(false);
            when(passwordEncoder.encode("newPass1234")).thenReturn("newEncodedPassword");

            UserUpdateRequest request = new UserUpdateRequest("currentPass", null, "newPass1234",1);

            userService.updateInfo(1L, request);

            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        }

        @Test
        @DisplayName("성공 - 닉네임, 비밀번호 모두 변경")
        void success_both() {
            User user = User.of("testUser", "oldEncodedPassword", "oldNick");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("currentPass", "oldEncodedPassword")).thenReturn(true);
            when(passwordEncoder.matches("newPass1234", "oldEncodedPassword")).thenReturn(false);
            when(passwordEncoder.encode("newPass1234")).thenReturn("newEncodedPassword");
            when(userRepository.existsByNickname("newNick")).thenReturn(false);

            UserUpdateRequest request = new UserUpdateRequest("currentPass", "newNick", "newPass1234",1);

            UserUpdateResponse result = userService.updateInfo(1L, request);

            assertThat(result.nickname()).isEqualTo("newNick");
            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        }

        @Test
        @DisplayName("성공 - 현재 닉네임과 동일한 경우 중복 체크 skip")
        void success_sameNickname() {
            User user = User.of("testUser", "encodedPassword", "tester");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);

            UserUpdateRequest request = new UserUpdateRequest("currentPass", "tester", null,1);

            UserUpdateResponse result = userService.updateInfo(1L, request);

            assertThat(result.nickname()).isEqualTo("tester");
            verify(userRepository, never()).existsByNickname(anyString());
        }

        @Test
        @DisplayName("성공 - OAuth 유저는 현재 비밀번호 검증을 스킵한다")
        void success_oauthUser_skipsPasswordVerification() {
            User oauthUser = User.ofSocial("test@google.com", "tester", AuthProvider.GOOGLE, "google123");
            ReflectionTestUtils.setField(oauthUser, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(oauthUser));
            when(userRepository.existsByNickname("newNick")).thenReturn(false);

            UserUpdateRequest request = new UserUpdateRequest(null, "newNick", null,1);

            UserUpdateResponse result = userService.updateInfo(1L, request);

            assertThat(result.nickname()).isEqualTo("newNick");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("실패 - 현재 비밀번호가 null이면 INVALID_PASSWORD 예외가 발생한다")
        void fail_currentPasswordNull() {
            User user = User.of("testUser", "encodedPassword", "tester");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserUpdateRequest request = new UserUpdateRequest(null, "newNick", null,1);

            assertThatThrownBy(() -> userService.updateInfo(1L, request))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.INVALID_PASSWORD));
        }

        @Test
        @DisplayName("실패 - 현재 비밀번호가 일치하지 않으면 PASSWORD_MISMATCH 예외가 발생한다")
        void fail_currentPasswordMismatch() {
            User user = User.of("testUser", "encodedPassword", "tester");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

            UserUpdateRequest request = new UserUpdateRequest("wrongPass", "newNick", null,1);

            assertThatThrownBy(() -> userService.updateInfo(1L, request))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.PASSWORD_MISMATCH));
        }

        @Test
        @DisplayName("실패 - 새 비밀번호가 현재 비밀번호와 동일하면 SAME_AS_CURRENT_PASSWORD 예외가 발생한다")
        void fail_sameAsCurrentPassword() {
            User user = User.of("testUser", "encodedPassword", "tester");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true); // 새 비밀번호도 같음

            UserUpdateRequest request = new UserUpdateRequest("currentPass", null, "currentPass",1);

            assertThatThrownBy(() -> userService.updateInfo(1L, request))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.SAME_AS_CURRENT_PASSWORD));
        }

        @Test
        @DisplayName("실패 - 닉네임 중복이면 NICKNAME_ALREADY_EXIST 예외가 발생한다")
        void fail_duplicateNickname() {
            User user = User.of("testUser", "encodedPassword", "oldNick");
            ReflectionTestUtils.setField(user, "id", 1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
            when(userRepository.existsByNickname("takenNick")).thenReturn(true);

            UserUpdateRequest request = new UserUpdateRequest("currentPass", "takenNick", null,1);

            assertThatThrownBy(() -> userService.updateInfo(1L, request))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.NICKNAME_ALREADY_EXIST));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
        void fail_userNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UserUpdateRequest request = new UserUpdateRequest("currentPass", "newNick", null,1);

            assertThatThrownBy(() -> userService.updateInfo(1L, request))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_NOT_FOUND));
        }
    }
}
