package com.eof.back.global.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles("test")
class RedisTokenVersionStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisTokenVersionStore store;

    private static final Long USER_ID = 1L;
    private static final String KEY = "token:version:1";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new RedisTokenVersionStore(redisTemplate);
    }

    @Nested
    @DisplayName("increment")
    class Increment {

        @Test
        @DisplayName("Redis INCR 결과를 그대로 반환한다")
        void returnsIncrementedValue() {
            given(valueOperations.increment(KEY)).willReturn(3L);

            long result = store.increment(USER_ID);

            assertThat(result).isEqualTo(3L);
            verify(valueOperations).increment(KEY);
        }

        @Test
        @DisplayName("최초 로그인 시 INCR은 1을 반환한다 (키 없으면 0 초기화 후 +1)")
        void firstLogin_returnsOne() {
            given(valueOperations.increment(KEY)).willReturn(1L);

            long result = store.increment(USER_ID);

            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("increment가 null을 반환하는 경우 1을 반환한다 (NPE 방어)")
        void nullResult_returnsOne() {
            given(valueOperations.increment(KEY)).willReturn(null);

            long result = store.increment(USER_ID);

            assertThat(result).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("키가 존재하면 저장된 version을 Optional로 반환한다")
        void keyExists_returnsVersion() {
            given(valueOperations.get(KEY)).willReturn("5");

            Optional<Long> result = store.findByUserId(USER_ID);

            assertThat(result).isPresent().hasValue(5L);
        }

        @Test
        @DisplayName("키가 없으면 Optional.empty를 반환한다 (로그아웃/탈퇴 상태)")
        void keyAbsent_returnsEmpty() {
            given(valueOperations.get(KEY)).willReturn(null);

            Optional<Long> result = store.findByUserId(USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("해당 userId의 tokenVersion 키를 삭제한다")
        void deletesKey() {
            store.delete(USER_ID);

            verify(redisTemplate).delete(KEY);
        }
    }
}
