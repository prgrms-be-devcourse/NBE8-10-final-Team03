package com.eof.back.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
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
     * username을 SHA-256으로 해시하여 고정 길이(64자) Redis 키를 생성합니다.
     * 원문 username을 키에 직접 노출하지 않고, 임의 길이 입력으로 인한 메모리 낭비를 방지합니다.
     */
    private String buildKey(String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(username.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    /**
     * 현재 계정이 잠금 상태인지 확인합니다.
     *
     * @param username 사용자 아이디
     * @return 실패 횟수가 최대치 이상이면 true
     */
    public boolean isLocked(String username) {
        String value = redisTemplate.opsForValue().get(buildKey(username));
        if (value == null) return false;
        return Integer.parseInt(value) >= MAX_ATTEMPTS;
    }

    /**
     * 로그인 실패를 기록합니다.
     * 첫 실패는 SET NX EX로 값과 TTL을 원자적으로 설정하고,
     * 이후 실패는 INCR로 카운트만 증가시킵니다.
     * 잠금 도달(MAX_ATTEMPTS) 시 TTL을 재설정하여 15분 잠금을 보장합니다.
     *
     * @param username 사용자 아이디
     */
    public void recordFailure(String username) {
        String key = buildKey(username);
        // 키가 없을 때만 "1"로 원자적 설정 (SET NX EX) — INCR 후 EXPIRE 사이 재시작으로 인한 영구 키 방지
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isNew)) {
            return;
        }
        // 키가 이미 있으면 카운트만 증가
        Long count = redisTemplate.opsForValue().increment(key);
        // 잠금 도달 시 TTL 재설정으로 15분 잠금 보장
        if (count != null && count == MAX_ATTEMPTS) {
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
        String value = redisTemplate.opsForValue().get(buildKey(username));
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * 로그인 성공 시 실패 카운터를 초기화합니다.
     *
     * @param username 사용자 아이디
     */
    public void resetAttempts(String username) {
        redisTemplate.delete(buildKey(username));
    }
}
