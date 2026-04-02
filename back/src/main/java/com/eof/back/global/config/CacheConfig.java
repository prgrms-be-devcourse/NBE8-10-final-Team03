package com.eof.back.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 기반 캐시 설정 클래스입니다.
 * <p>
 * {@link RedisCacheManager}를 빈으로 등록하여 {@code @Cacheable} 어노테이션이
 * 붙은 메서드의 결과를 Redis에 캐싱합니다.
 *
 * <p><b>캐시 목록 및 TTL:</b><br>
 * {@code ranking:all} - 전체 랭킹 TOP10 (1분) <br>
 * {@code ranking:weekly} - 주간 랭킹 TOP10 (5분) <br>
 * {@code ranking:monthly} - 월간 랭킹 TOP10 (10분) <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Configuration}으로 등록되며, {@code @EnableCaching}으로 캐시 AOP를 활성화합니다.
 *
 * @author Jaewon Ryu
 * @since 2026-04-01
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new JdkSerializationRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("ranking:all", defaultConfig.entryTtl(Duration.ofMinutes(3)));
        cacheConfigs.put("ranking:weekly", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("ranking:monthly", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}