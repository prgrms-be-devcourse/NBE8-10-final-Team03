package com.eof.back.global.jwt;

import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰의 생성, 검증, 파싱을 담당하는 클래스입니다.
 *
 * <p>AccessToken / RefreshToken을 생성하며,
 * 토큰에는 사용자 식별 정보와 만료 시간(expiration)이 포함됩니다.
 *
 * <p><b>주요 역할:</b>
 * - AccessToken 생성 (인증용)
 * - RefreshToken 생성 (재발급용)
 * - 토큰 검증 및 Claims 파싱
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
     * <p>{@code tokenVersion}은 {@link com.eof.back.global.token.TokenVersionStore}에서 관리하는 값으로,
     * 로그인 시 증가된 버전을 claim에 포함합니다.
     * 필터에서 Redis 저장 값과 비교하여 버전이 다르면 인증을 거부합니다.
     *
     * @param userId       사용자 ID (subject로 사용됨)
     * @param username     사용자 아이디
     * @param role         사용자 권한 (USER, ADMIN 등)
     * @param nickname     사용자 닉네임
     * @param tokenVersion 다중 로그인 제어 및 즉시 무효화를 위한 버전 값
     * @return JWT AccessToken 문자열
     */
    public String createAccessToken(Long userId, String username, String role, String nickname, long tokenVersion) {

        // 현재 시간
        Date now = new Date();
        // 만료 시간 = 현재 시간 + 설정된 만료 시간
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))          // subject: 토큰의 주체 (보통 사용자 식별값)
                .claim("username", username)              // 사용자 아이디를 claim으로 추가
                .claim("role", role)                      // 사용자 권한 정보 추가
                .claim("nickname", nickname)              // 사용할 닉네임 추가
                .claim("tokenVersion", tokenVersion)      // 다중 로그인 제어 및 즉시 무효화용 버전
                .issuedAt(now)                            // 토큰 발급 시간
                .expiration(expiry)                       // 토큰 만료 시간
                .signWith(secretKey)                      // secretKey로 서명 (위조 방지 핵심)
                .compact();                               // 최종적으로 JWT 문자열 생성
    }

    /**
     * RefreshToken 생성
     *
     * <p>AccessToken이 만료되었을 때 재발급을 위한 토큰입니다.
     * 보통 AccessToken보다 만료 시간이 길게 설정됩니다.
     *
     * <p>{@code tokenVersion}을 claim에 포함하여, reissue 시 Redis 저장 값과 비교합니다.
     * 다른 기기에서 재로그인하여 version이 증가한 경우 이 refresh token으로는 재발급이 거부됩니다.
     *
     * @param userId       사용자 ID
     * @param username     사용자 아이디
     * @param role         사용자 권한
     * @param nickname     사용자 닉네임
     * @param tokenVersion 발급 시점의 tokenVersion
     * @return JWT RefreshToken 문자열
     */
    public String createRefreshToken(Long userId, String username, String role, String nickname, long tokenVersion) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .claim("nickname", nickname)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 Claims를 추출합니다.
     *
     * <p>서명 검증이 함께 수행되며, 유효하지 않은 토큰이면 예외가 발생합니다.
     *
     * @param token JWT 토큰
     * @return 토큰 내부 Claims 정보
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)         // 해당 secretKey로 서명을 검증합니다.
                .build()
                .parseSignedClaims(token)      // 서명된 JWT를 파싱합니다.
                .getPayload();                 // payload(Claims)를 반환합니다.
    }

    /**
     * 토큰의 유효성을 검증하고 Claims를 반환합니다.
     *
     * <p>서명, 형식, 만료 여부를 확인합니다.
     * 유효하지 않은 경우 {@link AuthException}을 발생시킵니다.
     *
     * @param token JWT 토큰
     * @return 토큰 내부 Claims 정보
     * @throws AuthException 토큰이 만료되었거나 유효하지 않은 경우
     */
    public Claims validateToken(String token) {
        try {
            return getClaims(token);
        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * Claims에서 사용자 ID를 추출합니다.
     *
     * @param claims JWT Claims
     * @return 사용자 ID
     */
    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    /**
     * Claims에서 사용자 아이디를 추출합니다.
     *
     * @param claims JWT Claims
     * @return 사용자 아이디
     */
    public String getUsername(Claims claims) {
        String value = claims.get("username", String.class);
        if (value == null) throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        return value;
    }

    /**
     * Claims에서 사용자 권한을 추출합니다.
     *
     * @param claims JWT Claims
     * @return 사용자 권한
     */
    public String getRole(Claims claims) {
        String value = claims.get("role", String.class);
        if (value == null) throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        return value;
    }

    /**
     * Claims에서 사용자 닉네임을 추출합니다.
     *
     * @param claims JWT Claims
     * @return 사용자 닉네임
     */
    public String getNickname(Claims claims) {
        String value = claims.get("nickname", String.class);
        if (value == null) throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        return value;
    }

    /**
     * Claims에서 tokenVersion을 추출합니다.
     *
     * <p>tokenVersion claim이 없는 경우는 이 기능 도입 이전에 발급된 토큰이거나
     * 위변조된 토큰입니다. 두 경우 모두 인증 실패로 처리합니다.
     *
     * @param claims JWT Claims
     * @return tokenVersion
     * @throws com.eof.back.global.exception.exceptions.AuthException claim이 없는 경우
     */
    public long getTokenVersion(Claims claims) {
        Long value = claims.get("tokenVersion", Long.class);
        if (value == null) throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        return value;
    }
}
