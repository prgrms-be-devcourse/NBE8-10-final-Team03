package com.eof.back.domain.user.service;

import com.eof.back.domain.user.dto.UserInfoResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        UserInfoResponse result = userService.getMyInfo(1L);

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
        assertThatThrownBy(() -> userService.getMyInfo(1L))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.USER_NOT_FOUND));

        verify(userRepository).findById(1L);
    }
}
