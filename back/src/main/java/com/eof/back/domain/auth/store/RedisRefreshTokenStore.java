package com.eof.back.domain.auth.store;

import com.eof.back.domain.auth.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 Refresh Token 저장소 구현체입니다.
 * <p>
 * RefreshTokenStore 인터페이스를 구현하며,
 * Refresh Token의 저장, 조회, 삭제 작업을 Redis를 통해 수행합니다.
 * <p>
 * Redis의 TTL(Time To Live) 기능을 활용하여 만료 시간을 관리하므로
 * 별도의 스케줄러 없이 자동으로 만료된 토큰이 삭제됩니다.
 * <p>
 * {@code custom.refresh-token.store=redis} 설정 시 활성화됩니다.
 *
 * <p><b>키 구조:</b><br>
 * {@code refresh_token:{userId}} → token 문자열
 *
 * @author 5h6vm
 * @since 2026-03-26
 */
@Component
@ConditionalOnProperty(name = "custom.refresh-token.store", havingValue = "redis")
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh_token:";
    private static final LocalDateTime REDIS_TTL_MANAGED = LocalDateTime.MAX;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(Long userId, String token, LocalDateTime expiredAt) {
        String key = KEY_PREFIX + userId;
        // 현재 시각 기준으로 남은 초를 계산해 Redis TTL로 설정
        // TTL이 만료되면 Redis가 키를 자동 삭제하므로 별도 스케줄러가 필요 없음
        long ttlSeconds = Duration.between(LocalDateTime.now(), expiredAt).toSeconds();
        redisTemplate.opsForValue().set(key, token, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        String key = KEY_PREFIX + userId;
        String token = redisTemplate.opsForValue().get(key);

        // 키가 없으면 만료됐거나 존재하지 않는 토큰
        // Redis가 TTL 기반으로 자동 삭제하므로 키가 존재하면 유효한 토큰임이 보장됨
        if (token == null) {
            return Optional.empty();
        }

        // RefreshToken은 JPA 엔티티지만 여기서는 DB에 저장하지 않고
        // Redis에서 가져온 데이터를 담는 DTO처럼 메모리 상에서 조립해 반환
        // expiredAt은 서비스 계층의 isExpired() 검증을 통과시키기 위해 미래 시각으로 설정
        // 실제 만료는 Redis TTL이 보장하므로 정확한 잔여 시간 계산이 불필요
        return Optional.of(RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiredAt(REDIS_TTL_MANAGED)
                .build());
    }

    @Override
    public void delete(Long userId) {
        // 로그아웃/회원탈퇴 시 즉시 키 삭제 → 해당 토큰으로 재발급 불가
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
