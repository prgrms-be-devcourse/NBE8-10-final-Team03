package com.eof.back.global.jwt;

import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import io.jsonwebtoken.Claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
class JwtTokenProviderTest {

    private static final String SECRET =
            "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, 1200, 1209600);
    }

    @Test
    @DisplayName("AccessToken 생성 후 userId, username, role, tokenVersion 추출 성공")
    void createAccessToken_extractClaims_success() {
        String token = jwtTokenProvider.createAccessToken(1L, "testUser", "USER", "tester", 3L);
        Claims claims = jwtTokenProvider.validateToken(token);

        assertThat(jwtTokenProvider.getUserId(claims)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getUsername(claims)).isEqualTo("testUser");
        assertThat(jwtTokenProvider.getRole(claims)).isEqualTo("USER");
        assertThat(jwtTokenProvider.getTokenVersion(claims)).isEqualTo(3L);
    }

    @Test
    @DisplayName("RefreshToken 생성 후 userId, username, role, nickname, tokenVersion 추출 성공")
    void createRefreshToken_extractClaims_success() {
        String token = jwtTokenProvider.createRefreshToken(2L, "testUser", "USER", "tester", 5L);
        Claims claims = jwtTokenProvider.validateToken(token);

        assertThat(jwtTokenProvider.getUserId(claims)).isEqualTo(2L);
        assertThat(jwtTokenProvider.getUsername(claims)).isEqualTo("testUser");
        assertThat(jwtTokenProvider.getRole(claims)).isEqualTo("USER");
        assertThat(jwtTokenProvider.getNickname(claims)).isEqualTo("tester");
        assertThat(jwtTokenProvider.getTokenVersion(claims)).isEqualTo(5L);
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공 - Claims 반환")
    void validateToken_success() {
        String token = jwtTokenProvider.createAccessToken(1L, "testUser", "USER", "tester", 1L);

        Claims claims = jwtTokenProvider.validateToken(token);

        assertThat(claims.get("username", String.class)).isEqualTo("testUser");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 TOKEN_EXPIRED 예외 발생")
    void validateToken_expired_throwsException() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1, -1);
        String token = expiredProvider.createAccessToken(1L, "testUser", "USER", "tester", 1L);

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
        String token = otherProvider.createAccessToken(1L, "testUser", "USER", "tester", 1L);

        assertThatThrownBy(() -> jwtTokenProvider.validateToken(token))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.TOKEN_INVALID));
    }

    @Test
    @DisplayName("tokenVersion claim이 없는 토큰에서 getTokenVersion 호출 시 TOKEN_INVALID 예외 발생")
    void getTokenVersion_missingClaim_throwsException() {
        // tokenVersion이 없는 토큰 = 이 기능 도입 이전에 발급된 토큰 또는 위변조된 토큰
        // 현재 provider가 tokenVersion을 항상 포함하므로, 다른 secret으로 수동 생성하는 대신
        // claims.get()이 null을 반환하는 상황을 mock으로 재현
        io.jsonwebtoken.Claims claims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
        org.mockito.Mockito.when(claims.get("tokenVersion", Long.class)).thenReturn(null);

        assertThatThrownBy(() -> jwtTokenProvider.getTokenVersion(claims))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.TOKEN_INVALID));
    }
}
