package com.eof.back.domain.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private static final String KEY_PREFIX = "login:fail:";
    private static final long LOCK_MINUTES = 15;

    private String buildKey(String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(username.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nested
    @DisplayName("isLocked")
    class IsLocked {

        @Test
        @DisplayName("실패 횟수가 없으면 false를 반환한다")
        void notLocked_whenNoRecord() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(buildKey("testUser"))).willReturn(null);

            assertThat(loginAttemptService.isLocked("testUser")).isFalse();
        }

        @Test
        @DisplayName("실패 횟수가 5 미만이면 false를 반환한다")
        void notLocked_whenBelowMax() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(buildKey("testUser"))).willReturn("4");

            assertThat(loginAttemptService.isLocked("testUser")).isFalse();
        }

        @Test
        @DisplayName("실패 횟수가 5 이상이면 true를 반환한다")
        void locked_whenAtMax() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(buildKey("testUser"))).willReturn("5");

            assertThat(loginAttemptService.isLocked("testUser")).isTrue();
        }
    }

    @Nested
    @DisplayName("recordFailure")
    class RecordFailure {

        @Test
        @DisplayName("첫 번째 실패 시 SET NX EX로 값과 TTL을 원자적으로 설정한다")
        void setsValueAndTtlAtomically_onFirstFailure() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.setIfAbsent(buildKey("testUser"), "1", LOCK_MINUTES, TimeUnit.MINUTES))
                    .willReturn(true);

            loginAttemptService.recordFailure("testUser");

            verify(valueOperations).setIfAbsent(buildKey("testUser"), "1", LOCK_MINUTES, TimeUnit.MINUTES);
            verify(valueOperations, never()).increment(buildKey("testUser"));
        }

        @Test
        @DisplayName("중간 실패(2~4회)에는 카운트만 증가시키고 TTL을 재설정하지 않는다")
        void onlyIncrements_onMiddleFailures() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.setIfAbsent(buildKey("testUser"), "1", LOCK_MINUTES, TimeUnit.MINUTES))
                    .willReturn(false);
            given(valueOperations.increment(buildKey("testUser"))).willReturn(3L);

            loginAttemptService.recordFailure("testUser");

            verify(valueOperations).increment(buildKey("testUser"));
            verify(redisTemplate, never()).expire(eq(buildKey("testUser")), eq(LOCK_MINUTES), eq(TimeUnit.MINUTES));
        }

        @Test
        @DisplayName("잠금 도달(5회) 시 TTL을 재설정하여 15분 잠금을 보장한다")
        void resetsTtl_onMaxAttempts() {
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.setIfAbsent(buildKey("testUser"), "1", LOCK_MINUTES, TimeUnit.MINUTES))
                    .willReturn(false);
            given(valueOperations.increment(buildKey("testUser"))).willReturn(5L);

            loginAttemptService.recordFailure("testUser");

            verify(redisTemplate).expire(buildKey("testUser"), LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Nested
    @DisplayName("resetAttempts")
    class ResetAttempts {

        @Test
        @DisplayName("성공 시 Redis 키를 삭제한다")
        void deletesKey() {
            loginAttemptService.resetAttempts("testUser");

            verify(redisTemplate).delete(buildKey("testUser"));
        }
    }
}
