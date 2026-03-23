package com.eof.back.global.jwt;

import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET =
            "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, 1200, 1209600);
    }

    @Test
    @DisplayName("AccessToken 생성 후 userId, username, role 추출 성공")
    void createAccessToken_extractClaims_success() {
        String token = jwtTokenProvider.createAccessToken(1L, "testUser", "USER");
        Claims claims = jwtTokenProvider.validateToken(token);

        assertThat(jwtTokenProvider.getUserId(claims)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getUsername(claims)).isEqualTo("testUser");
        assertThat(jwtTokenProvider.getRole(claims)).isEqualTo("USER");
    }

    @Test
    @DisplayName("RefreshToken 생성 후 userId 추출 성공")
    void createRefreshToken_extractUserId_success() {
        String token = jwtTokenProvider.createRefreshToken(2L);
        Claims claims = jwtTokenProvider.validateToken(token);

        assertThat(jwtTokenProvider.getUserId(claims)).isEqualTo(2L);
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공 - Claims 반환")
    void validateToken_success() {
        String token = jwtTokenProvider.createAccessToken(1L, "testUser", "USER");

        Claims claims = jwtTokenProvider.validateToken(token);

        assertThat(claims.get("username", String.class)).isEqualTo("testUser");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 TOKEN_EXPIRED 예외 발생")
    void validateToken_expired_throwsException() {
        // 만료 시간을 음수로 설정하여 즉시 만료된 토큰 생성
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1, -1);
        String token = expiredProvider.createAccessToken(1L, "testUser", "USER");

        assertThatThrownBy(() -> jwtTokenProvider.validateToken(token))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.TOKEN_EXPIRED));
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 시 TOKEN_INVALID 예외 발생")
    void validateToken_invalid_throwsException() {
        assertThatThrownBy(() -> jwtTokenProvider.validateToken("invalid.token.value"))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.TOKEN_INVALID));
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰 검증 시 TOKEN_INVALID 예외 발생")
    void validateToken_differentSecret_throwsException() {
        String otherSecret =
                "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherSecret, 1200, 1209600);
        String token = otherProvider.createAccessToken(1L, "testUser", "USER");

        assertThatThrownBy(() -> jwtTokenProvider.validateToken(token))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.TOKEN_INVALID));
    }
}
