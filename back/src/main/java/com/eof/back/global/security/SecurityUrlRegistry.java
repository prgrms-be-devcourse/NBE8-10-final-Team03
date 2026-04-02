package com.eof.back.global.security;

import com.eof.back.global.config.SecurityConfig;
import org.springframework.http.HttpMethod;
import java.util.Map;

/**
 * Spring Security에서 접근 권한을 관리할 URL 목록을 정의하는 클래스입니다.
 * <p>
 * 애플리케이션의 엔드포인트 중 인증 없이 접근 가능한(Public) 경로들을 중앙 집중식으로 관리합니다.
 * 새로운 공개 API가 추가될 경우 이 클래스의 상수를 수정하면 {@link SecurityConfig}에 자동으로 반영됩니다.
 * </p>
 *
 * <p><b>주요 구성:</b><br>
 * - PERMIT_ALL_URLS: 모든 HTTP 메서드에 대해 허용할 경로 목록<br>
 * - PUBLIC_METHOD_URLS: 특정 HTTP 메서드(예: GET)에 대해서만 허용할 경로 목록
 * </p>
 *
 * @author MintyU
 * @since 2026-03-24
 */
public abstract class SecurityUrlRegistry {

    /**
     * 모든 HTTP 메서드에 대해 인증 없이 접근 가능한 URL 목록입니다.
     * Swagger 관련 문서 경로, 회원가입/로그인 등의 인증 관련 경로, 랭킹 조회 등의 일반 공개 API를 포함합니다.
     */
    public static final String[] PERMIT_ALL_URLS = {
            // Swagger 관련
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/v3/api-docs/**",

            // 인증 관련
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/reissue",

            // 소셜 로그인 관련 (OAuth2 인증 흐름)
            "/oauth2/**",
            "/login/oauth2/**",

            // 기타 공개 API
            "/api/v1/rankings",

            // 웹 소켓 관련
            "/ws/game"
    };

    /**
     * 특정 HTTP 메서드에 대해 인증 없이 접근 가능한 URL 목록입니다.
     * <p>
     * 키는 {@link HttpMethod}, 값은 해당 메서드로 허용할 경로들의 배열입니다.
     */
    public static final Map<HttpMethod, String[]> PUBLIC_METHOD_URLS = Map.of(
            HttpMethod.GET, new String[]{
                    "/api/v1/quizsets",
                    "/api/v1/quizsets/**"
            },
            HttpMethod.POST, new String[]{

            }
    );
}
