package com.eof.back.global.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Redis 기반 토큰 버전 저장소 구현체입니다.
 *
 * <p><b>설계 결정:</b><br>
 * TTL을 별도로 설정하지 않습니다.
 * 로그아웃·탈퇴·관리자 삭제 시 {@link #delete}로 키를 명시적으로 제거합니다.
 * Redis 재시작 등으로 키가 사라지면 해당 사용자는 다음 요청에서 인증 실패(401)를 받으며,
 * 재로그인 시 새 version이 생성됩니다. 게임 서비스 특성상 이 수준의 세션 초기화는 허용 가능합니다.
 *
 * <p><b>키 구조:</b><br>
 * {@code token:version:{userId}} → Long 문자열 (Redis는 숫자도 문자열로 저장)
 *
 * @author 5h6vm
 * @since 2026-04-02
 */
@Component
@RequiredArgsConstructor
public class RedisTokenVersionStore implements TokenVersionStore {

    private static final String KEY_PREFIX = "token:version:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Redis INCR 명령어로 version을 원자적으로 증가시킵니다.
     *
     * <p>INCR은 키가 없으면 0으로 초기화 후 1을 반환하므로,
     * 최초 로그인 시 별도 초기화 없이 version 1부터 시작합니다.
     * 동시 다중 로그인 요청이 들어와도 원자성이 보장됩니다.
     */
    @Override
    public long increment(Long userId) {
        Long version = redisTemplate.opsForValue().increment(KEY_PREFIX + userId);
        // increment가 null을 반환하는 경우는 정상적으로 발생하지 않지만 NPE 방지
        return version != null ? version : 1L;
    }

    /**
     * 저장된 tokenVersion을 조회합니다.
     *
     * <p>키가 없으면 {@link Optional#empty()}를 반환합니다.
     * 이는 로그아웃/탈퇴/관리자 삭제로 키가 제거된 상태이며,
     * 필터에서 인증 실패로 처리합니다.
     */
    @Override
    public Optional<Long> findByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(value));
    }

    /**
     * tokenVersion 키를 삭제합니다.
     *
     * <p>키 삭제 후 해당 사용자의 모든 access token은 다음 요청부터 인증 실패합니다.
     * 로그아웃·탈퇴·관리자 삭제 시 호출됩니다.
     */
    @Override
    public void delete(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
