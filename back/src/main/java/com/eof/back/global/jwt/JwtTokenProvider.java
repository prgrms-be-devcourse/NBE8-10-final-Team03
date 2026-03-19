package com.eof.back.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰을 생성하는 클래스입니다.
 *
 * <p>AccessToken / RefreshToken을 생성하며,
 * 토큰에는 사용자 식별 정보와 만료 시간(expiration)이 포함됩니다.
 *
 * <p><b>주요 역할:</b>
 * - AccessToken 생성 (인증용)
 * - RefreshToken 생성 (재발급용)
 *
 * @author 5h6vm
 * @since 2026-03-19
 */
@Component
public class JwtTokenProvider {
    // JWT 서명(Signature)에 사용할 비밀 키
    private final SecretKey secretKey;

    // AccessToken 만료 시간 (ms 단위)
    private final long accessTokenExpiration;

    // RefreshToken 만료 시간 (ms 단위)
    private final long refreshTokenExpiration;

    /**
     * 생성자에서 application.yml 값을 주입받아 초기화합니다.
     *
     * @param secret JWT 서명에 사용할 문자열 키
     * @param accessTokenExpiration AccessToken 만료 시간
     * @param refreshTokenExpiration RefreshToken 만료 시간
     */
    public JwtTokenProvider(
            @Value("${custom.jwt.secretKey}") String secret,
            @Value("${custom.jwt.accessTokenExpirationSeconds}") long accessTokenExpiration,
            @Value("${custom.jwt.refreshTokenExpirationSeconds}") long refreshTokenExpiration
    ) {
        // 문자열 secret을 바이트 배열로 변환 후 HMAC-SHA 키로 생성
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // 토큰 만료 시간 설정 (초 → 밀리초 변환)
        this.accessTokenExpiration = accessTokenExpiration * 1000L;
        this.refreshTokenExpiration = refreshTokenExpiration * 1000L;
    }

    /**
     * AccessToken 생성
     *
     * <p>사용자의 인증 정보를 담은 토큰을 생성합니다.
     * 이후 요청 시 Authorization 헤더에 포함되어 사용됩니다.
     *
     * @param userId 사용자 ID (subject로 사용됨)
     * @param username 사용자 아이디
     * @param role 사용자 권한 (USER, ADMIN 등)
     * @return JWT AccessToken 문자열
     */
    public String createAccessToken(Long userId, String username, String role) {

        // 현재 시간
        Date now = new Date();
        // 만료 시간 = 현재 시간 + 설정된 만료 시간
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))  // subject: 토큰의 주체 (보통 사용자 식별값)
                .claim("username", username)  // 사용자 아이디를 claim으로 추가
                .claim("role", role)          // 사용자 권한 정보 추가
                .issuedAt(now)                    // 토큰 발급 시간
                .expiration(expiry)               // 토큰 만료 시간
                .signWith(secretKey)              // secretKey로 서명 (위조 방지 핵심)
                .compact();                       // 최종적으로 JWT 문자열 생성
    }

    /**
     * RefreshToken 생성
     *
     * <p>AccessToken이 만료되었을 때 재발급을 위한 토큰입니다.
     * 보통 AccessToken보다 만료 시간이 길게 설정됩니다.
     *
     * @param userId 사용자 ID
     * @return JWT RefreshToken 문자열
     */
    public String createRefreshToken(Long userId) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))  // subject에 사용자 ID 저장
                .issuedAt(now)                    // 발급 시간
                .expiration(expiry)               // 만료 시간
                .signWith(secretKey)              // 서명
                .compact();                       // JWT 문자열 생성
    }
}
