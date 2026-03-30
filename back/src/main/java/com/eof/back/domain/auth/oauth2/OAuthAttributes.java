package com.eof.back.domain.auth.oauth2;

import com.eof.back.domain.user.user.entity.AuthProvider;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

/**
 * 소셜 제공자(Google, Kakao)로부터 받은 사용자 정보를 통일된 형태로 변환하는 클래스입니다.
 *
 * <p>Google과 Kakao는 사용자 정보 응답 구조가 서로 다릅니다.
 * 이 클래스는 제공자별 응답을 파싱하여 서비스 내부에서 사용할 수 있는
 * 단일 형태({@code providerId}, {@code email}, {@code nickname})로 정규화합니다.</p>
 *
 * <p>주요 목적:</p>
 * <ul>
 *     <li>Google 응답 파싱: {@code sub}(providerId), {@code email}, {@code name}(nickname)</li>
 *     <li>Kakao 응답 파싱: {@code id}(providerId), {@code kakao_account.email}, {@code profile.nickname}</li>
 * </ul>
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@Getter
public class OAuthAttributes {

    private final String providerId;
    private final String email;
    private final String nickname;
    private final AuthProvider provider;

    private OAuthAttributes(String providerId, String email, String nickname, AuthProvider provider) {
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
    }

    /**
     * 소셜 제공자 이름과 사용자 속성 Map을 받아 OAuthAttributes를 생성합니다.
     * Spring Security OAuth2가 제공자별로 사용자 정보를 Map 형태로 전달합니다.
     *
     * @param registrationId 소셜 제공자 식별자 (application.yml의 registration 키, 예: "google", "kakao")
     * @param attributes     소셜 제공자로부터 받은 사용자 정보 Map
     * @return 파싱된 OAuthAttributes
     */
    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> throw new RuntimeException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        };
    }

    /**
     * Google 응답을 파싱합니다.
     *
     * <p>Google 응답 구조:</p>
     * <pre>
     * {
     *   "sub": "123456789",   // 사용자 고유 ID
     *   "email": "user@gmail.com",
     *   "name": "홍길동"
     * }
     * </pre>
     */
    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return new OAuthAttributes(
                String.valueOf(attributes.get("sub")),
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                AuthProvider.GOOGLE
        );
    }

    /**
     * Kakao 응답을 파싱합니다.
     *
     * <p>Kakao 응답 구조:</p>
     * <pre>
     * {
     *   "id": 9876543,                          // 사용자 고유 ID
     *   "kakao_account": {
     *     "profile": {
     *       "nickname": "홍길동"
     *     }
     *   }
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = kakaoAccount != null
                ? (Map<String, Object>) kakaoAccount.get("profile")
                : null;

        if (kakaoAccount == null || profile == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_response"), "카카오 응답에서 필수 정보를 가져올 수 없습니다.");
        }

        return new OAuthAttributes(
                String.valueOf(attributes.get("id")),
                null,   // 카카오는 이메일 동의 없이 수집하지 않음
                (String) profile.get("nickname"),
                AuthProvider.KAKAO
        );
    }
}
