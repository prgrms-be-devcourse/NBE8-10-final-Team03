package com.eof.back.domain.auth.store;

import com.eof.back.domain.auth.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token 저장소의 동작을 정의하는 인터페이스
 * <p>
 * Refresh Token의 저장, 조회, 삭제 기능을 추상화하여
 * 저장소 구현 방식(DB, Redis 등)과 비즈니스 로직을 분리합니다.
 *
 * <p><b>주요 메서드:</b><br>
 * {@code save(Long userId, String token, LocalDateTime expiredAt)} <br>
 * 사용자 ID를 기준으로 Refresh Token과 만료 시간을 저장하거나 갱신합니다. <br><br>
 *
 * {@code findByUserId(Long userId)} <br>
 * 사용자 ID를 기준으로 저장된 Refresh Token 엔티티를 조회합니다. <br><br>
 *
 * {@code delete(Long userId)} <br>
 * 사용자 ID를 기준으로 저장된 Refresh Token을 삭제합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public interface RefreshTokenStore {

    void save(Long userId, String token, LocalDateTime expiredAt);

    Optional<RefreshToken> findByUserId(Long userId);

    void delete(Long userId);
}
