package com.eof.back.global.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * JWT 토큰을 HttpOnly 쿠키로 관리하는 유틸리티 클래스입니다.
 *
 * <p>HttpOnly 쿠키란 JavaScript에서 접근할 수 없는 쿠키입니다.
 * document.cookie로 읽을 수 없기 때문에 XSS 공격으로 토큰이 탈취되는 것을 방어합니다.</p>
 *
 * <p><b>주요 기능:</b><br>
 * - AccessToken / RefreshToken 쿠키 추가 및 삭제<br>
 * - 요청 쿠키에서 특정 이름의 토큰 값 추출
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@Component
public class CookieUtil {

    // 쿠키 이름 상수 — Filter나 Handler에서 이 이름으로 쿠키를 조회합니다
    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    // application.yml의 만료 시간 설정값을 주입받아 쿠키 maxAge와 동기화합니다
    private final int accessTokenMaxAge;
    private final int refreshTokenMaxAge;

    public CookieUtil(
            @Value("${custom.jwt.accessTokenExpirationSeconds}") int accessTokenMaxAge,
            @Value("${custom.jwt.refreshTokenExpirationSeconds}") int refreshTokenMaxAge
    ) {
        this.accessTokenMaxAge = accessTokenMaxAge;
        this.refreshTokenMaxAge = refreshTokenMaxAge;
    }

    /**
     * 응답에 accessToken 쿠키를 추가합니다.
     * 로그인 성공 시 호출됩니다.
     */
    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, ACCESS_TOKEN_COOKIE, token, accessTokenMaxAge);
    }

    /**
     * 응답에 refreshToken 쿠키를 추가합니다.
     * 로그인 성공 시 호출됩니다.
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, REFRESH_TOKEN_COOKIE, token, refreshTokenMaxAge);
    }

    /**
     * accessToken, refreshToken 쿠키를 한 번에 추가합니다.
     * 로그인, 토큰 재발급 시 호출됩니다.
     */
    public void addAllTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addAccessTokenCookie(response, accessToken);
        addRefreshTokenCookie(response, refreshToken);
    }

    /**
     * accessToken 쿠키를 삭제합니다.
     * 로그아웃 시 호출됩니다. maxAge를 0으로 설정하면 브라우저가 즉시 쿠키를 삭제합니다.
     */
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE);
    }

    /**
     * refreshToken 쿠키를 삭제합니다.
     * 로그아웃 시 호출됩니다.
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(response, REFRESH_TOKEN_COOKIE);
    }

    /**
     * accessToken, refreshToken 쿠키를 한 번에 삭제합니다.
     * 로그아웃, 회원탈퇴 시 호출됩니다.
     */
    public void deleteAllTokenCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
    }

    /**
     * 요청 쿠키에서 특정 이름의 쿠키 값을 꺼냅니다.
     * JwtAuthenticationFilter에서 accessToken을 추출할 때 사용합니다.
     *
     * @param request    HTTP 요청
     * @param cookieName 찾을 쿠키 이름
     * @return 쿠키 값 (없으면 Optional.empty())
     */
    public Optional<String> resolveToken(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * 실제 쿠키를 생성하고 응답에 추가하는 내부 메서드입니다.
     * ResponseCookie를 사용해 SameSite=Lax를 설정합니다.
     * SameSite=Lax: 같은 사이트 요청 + 외부에서 링크 클릭(GET)만 쿠키 포함 (CSRF 방어)
     *
     * @param name   쿠키 이름
     * @param value  쿠키 값 (JWT 토큰)
     * @param maxAge 쿠키 유효 시간 (초 단위, JWT 만료 시간과 동일하게 설정)
     */
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)     // JS 접근 차단 (XSS 방어)
                .path("/")          // 모든 경로에서 쿠키 전송
                .maxAge(maxAge)
                .sameSite("Lax")    // CSRF 방어
                // TODO: 운영 환경에서는 HTTPS 전용으로 secure(true) 활성화 필요
                // .secure(true)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 쿠키를 삭제하는 내부 메서드입니다.
     * maxAge를 0으로 설정하면 브라우저가 해당 쿠키를 즉시 만료 처리합니다.
     */
    private void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
