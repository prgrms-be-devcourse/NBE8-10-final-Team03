package com.eof.back.domain.auth.oauth2;

import com.eof.back.domain.user.user.entity.AuthProvider;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * CustomOAuth2UserService의 단위 테스트입니다.
 * 유저 조회/생성 및 닉네임 중복 처리 로직을 검증합니다.
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService userService;

    private OAuthAttributes googleAttributes() {
        return OAuthAttributes.of("google", Map.of(
                "sub", "google_123",
                "email", "test@gmail.com",
                "name", "홍길동"
        ));
    }

    @Nested
    @DisplayName("findOrCreateUser")
    class FindOrCreate {

        @Test
        @DisplayName("성공 - 기존 유저가 있으면 save를 호출하지 않는다")
        void existingUser_noSave() {
            User existingUser = mock(User.class);
            given(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google_123"))
                    .willReturn(Optional.of(existingUser));

            ReflectionTestUtils.invokeMethod(userService, "findOrCreateUser", googleAttributes());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공 - 신규 유저면 save를 호출한다")
        void newUser_save() {
            given(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google_123"))
                    .willReturn(Optional.empty());
            given(userRepository.existsByNickname("홍길동")).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(mock(User.class));

            ReflectionTestUtils.invokeMethod(userService, "findOrCreateUser", googleAttributes());

            verify(userRepository).save(argThat(user ->
                    user.getNickname().equals("홍길동") &&
                    user.getProvider() == AuthProvider.GOOGLE
            ));
        }
    }

    @Nested
    @DisplayName("generateUniqueNickname")
    class GenerateUniqueNickname {

        @Test
        @DisplayName("성공 - 닉네임 중복 없으면 그대로 반환한다")
        void noConflict() {
            given(userRepository.existsByNickname("홍길동")).willReturn(false);

            String result = ReflectionTestUtils.invokeMethod(userService, "generateUniqueNickname", "홍길동");

            assertThat(result).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("성공 - 닉네임 중복 시 _1 suffix를 붙인다")
        void conflict_addSuffix() {
            given(userRepository.existsByNickname("홍길동")).willReturn(true);
            given(userRepository.existsByNickname("홍길동_1")).willReturn(false);

            String result = ReflectionTestUtils.invokeMethod(userService, "generateUniqueNickname", "홍길동");

            assertThat(result).isEqualTo("홍길동_1");
        }

        @Test
        @DisplayName("성공 - _1도 중복이면 _2를 붙인다")
        void conflict_addSuffix2() {
            given(userRepository.existsByNickname("홍길동")).willReturn(true);
            given(userRepository.existsByNickname("홍길동_1")).willReturn(true);
            given(userRepository.existsByNickname("홍길동_2")).willReturn(false);

            String result = ReflectionTestUtils.invokeMethod(userService, "generateUniqueNickname", "홍길동");

            assertThat(result).isEqualTo("홍길동_2");
        }
    }

    // ReflectionTestUtils.invokeMethod 결과를 assertThat으로 검증하기 위한 헬퍼
    private static <T> org.assertj.core.api.AbstractStringAssert<?> assertThat(T actual) {
        return org.assertj.core.api.Assertions.assertThat((String) actual);
    }
}
