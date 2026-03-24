package com.eof.back.domain.user.service;

import com.eof.back.domain.user.dto.UserInfoResponse;
import com.eof.back.domain.user.dto.UserUpdateRequest;
import com.eof.back.domain.user.dto.UserUpdateResponse;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserService의 비즈니스 로직을 검증하는 단위 테스트 클래스입니다.
 *
 * <p>Mockito를 사용하여 의존 객체(UserRepository)를 Mock 처리하고
 * 서비스 계층의 로직이 의도한 대로 동작하는지 검증합니다.</p>
 *
 * <p>테스트 범위:</p>
 * <ul>
 *     <li>내 정보 조회 - 성공, 존재하지 않는 사용자</li>
 * </ul>
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_success() {
        // given
        User user = User.of("testUser", "encodedPassword", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserInfoResponse result = userService.getInfo(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("testUser");
        assertThat(result.nickname()).isEqualTo("tester");
        assertThat(result.role()).isEqualTo("USER");

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 존재하지 않는 사용자")
    void getMyInfo_fail_userNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getInfo(1L))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.USER_NOT_FOUND));

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("내 정보 수정 성공 - 닉네임만 변경")
    void updateInfo_success_nicknameOnly() {
        // given
        User user = User.of("testUser", "encodedPassword", "oldNick");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNick")).thenReturn(false);

        UserUpdateRequest request = new UserUpdateRequest("newNick", null);

        // when
        UserUpdateResponse result = userService.updateInfo(1L, request);

        // then
        assertThat(result.nickname()).isEqualTo("newNick");
        verify(userRepository).existsByNickname("newNick");
    }

    @Test
    @DisplayName("내 정보 수정 성공 - 비밀번호만 변경")
    void updateInfo_success_passwordOnly() {
        // given
        User user = User.of("testUser", "oldEncodedPassword", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass1234")).thenReturn("newEncodedPassword");

        UserUpdateRequest request = new UserUpdateRequest(null, "newPass1234");

        // when
        userService.updateInfo(1L, request);

        // then
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        verify(passwordEncoder).encode("newPass1234");
    }

    @Test
    @DisplayName("내 정보 수정 성공 - 닉네임, 비밀번호 모두 변경")
    void updateInfo_success_both() {
        // given
        User user = User.of("testUser", "oldEncodedPassword", "oldNick");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNick")).thenReturn(false);
        when(passwordEncoder.encode("newPass1234")).thenReturn("newEncodedPassword");

        UserUpdateRequest request = new UserUpdateRequest("newNick", "newPass1234");

        // when
        UserUpdateResponse result = userService.updateInfo(1L, request);

        // then
        assertThat(result.nickname()).isEqualTo("newNick");
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    @DisplayName("내 정보 수정 성공 - 현재 닉네임과 동일한 경우 중복 체크 skip")
    void updateInfo_success_sameNickname() {
        // given
        User user = User.of("testUser", "encodedPassword", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest("tester", null);

        // when
        UserUpdateResponse result = userService.updateInfo(1L, request);

        // then
        assertThat(result.nickname()).isEqualTo("tester");
        verify(userRepository, org.mockito.Mockito.never()).existsByNickname(anyString());
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 닉네임 중복")
    void updateInfo_fail_duplicateNickname() {
        // given
        User user = User.of("testUser", "encodedPassword", "oldNick");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("takenNick")).thenReturn(true);

        UserUpdateRequest request = new UserUpdateRequest("takenNick", null);

        // when & then
        assertThatThrownBy(() -> userService.updateInfo(1L, request))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.NICKNAME_ALREADY_EXIST));
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 존재하지 않는 사용자")
    void updateInfo_fail_userNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserUpdateRequest request = new UserUpdateRequest("newNick", null);

        // when & then
        assertThatThrownBy(() -> userService.updateInfo(1L, request))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.USER_NOT_FOUND));
    }
}
