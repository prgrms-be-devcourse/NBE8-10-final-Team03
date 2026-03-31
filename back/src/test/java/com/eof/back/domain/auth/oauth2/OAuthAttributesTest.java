package com.eof.back.domain.auth.oauth2;

import com.eof.back.domain.user.user.entity.AuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OAuthAttributes의 단위 테스트입니다.
 * Google, Kakao 응답 파싱 및 email null 처리를 검증합니다.
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
class OAuthAttributesTest {

    @Nested
    @DisplayName("Google 응답 파싱")
    class Google {

        @Test
        @DisplayName("성공 - Google 응답을 올바르게 파싱한다")
        void success() {
            Map<String, Object> attrs = Map.of(
                    "sub", "google_123",
                    "email", "test@gmail.com",
                    "name", "홍길동"
            );

            OAuthAttributes result = OAuthAttributes.of("google", attrs);

            assertThat(result.getProviderId()).isEqualTo("google_123");
            assertThat(result.getEmail()).isEqualTo("test@gmail.com");
            assertThat(result.getNickname()).isEqualTo("홍길동");
            assertThat(result.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        }

        @Test
        @DisplayName("성공 - registrationId 대소문자 무관하게 파싱한다")
        void caseInsensitive() {
            Map<String, Object> attrs = Map.of(
                    "sub", "google_123",
                    "email", "test@gmail.com",
                    "name", "홍길동"
            );

            OAuthAttributes result = OAuthAttributes.of("GOOGLE", attrs);

            assertThat(result.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        }
    }

    @Nested
    @DisplayName("Kakao 응답 파싱")
    class Kakao {

        @Test
        @DisplayName("성공 - Kakao 응답을 올바르게 파싱한다")
        void success() {
            Map<String, Object> profile = Map.of("nickname", "카카오유저");
            Map<String, Object> kakaoAccount = Map.of("profile", profile);
            Map<String, Object> attrs = Map.of("id", 9876543L, "kakao_account", kakaoAccount);

            OAuthAttributes result = OAuthAttributes.of("kakao", attrs);

            assertThat(result.getProviderId()).isEqualTo("9876543");
            assertThat(result.getEmail()).isNull();  // 카카오는 이메일 수집 안 함
            assertThat(result.getNickname()).isEqualTo("카카오유저");
            assertThat(result.getProvider()).isEqualTo(AuthProvider.KAKAO);
        }

        @Test
        @DisplayName("실패 - kakao_account가 없으면 OAuth2AuthenticationException이 발생한다")
        void fail_nullKakaoAccount() {
            Map<String, Object> attrs = Map.of("id", 9876543L);

            assertThatThrownBy(() -> OAuthAttributes.of("kakao", attrs))
                    .isInstanceOf(OAuth2AuthenticationException.class);
        }

        @Test
        @DisplayName("실패 - profile이 없으면 OAuth2AuthenticationException이 발생한다")
        void fail_nullProfile() {
            Map<String, Object> kakaoAccount = new HashMap<>();
            // profile 없음
            Map<String, Object> attrs = Map.of("id", 9876543L, "kakao_account", kakaoAccount);

            assertThatThrownBy(() -> OAuthAttributes.of("kakao", attrs))
                    .isInstanceOf(OAuth2AuthenticationException.class);
        }
    }

    @Nested
    @DisplayName("지원하지 않는 provider")
    class Unsupported {

        @Test
        @DisplayName("실패 - 지원하지 않는 provider면 OAuth2AuthenticationException이 발생한다")
        void fail_unsupportedProvider() {
            assertThatThrownBy(() -> OAuthAttributes.of("naver", Map.of()))
                    .isInstanceOf(OAuth2AuthenticationException.class);
        }
    }
}
