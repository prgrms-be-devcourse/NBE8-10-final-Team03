package com.eof.back.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 로그인 실패 횟수를 Redis로 관리하여 무차별 대입 공격을 방어합니다.
 *
 * <p><b>키 구조:</b><br>
 * {@code login:fail:{username}} → 실패 횟수 (문자열)
 *
 * <p>최대 실패 횟수 초과 시 일정 시간 동안 로그인이 차단됩니다.
 * 로그인 성공 시 카운터가 초기화됩니다.
 *
 * @author 5h6vm
 * @since 2026-04-01
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY_PREFIX = "login:fail:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final StringRedisTemplate redisTemplate;

    /**
     * 현재 계정이 잠금 상태인지 확인합니다.
     *
     * @param username 사용자 아이디
     * @return 실패 횟수가 최대치 이상이면 true
     */
    public boolean isLocked(String username) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + username);
        if (value == null) return false;
        return Integer.parseInt(value) >= MAX_ATTEMPTS;
    }

    /**
     * 로그인 실패를 기록합니다.
     * 첫 실패 시 TTL을 설정하고, 이후 실패는 카운트만 증가합니다.
     *
     * @param username 사용자 아이디
     */
    public void recordFailure(String username) {
        String key = KEY_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);
        // 첫 번째 실패 시 TTL 설정
        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 현재 로그인 실패 횟수를 반환합니다.
     *
     * @param username 사용자 아이디
     * @return 실패 횟수 (없으면 0)
     */
    public int getAttemptCount(String username) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + username);
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * 로그인 성공 시 실패 카운터를 초기화합니다.
     *
     * @param username 사용자 아이디
     */
    public void resetAttempts(String username) {
        redisTemplate.delete(KEY_PREFIX + username);
    }
}
