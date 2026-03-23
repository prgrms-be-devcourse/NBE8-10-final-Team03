package com.eof.back.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token을 관리하는 엔티티입니다.
 * <p>
 * 사용자 인증 과정에서 발급되는 Refresh Token을 저장하며,
 * Access Token 재발급 요청 시 서버에서 토큰의 유효성을 검증하는 데 사용됩니다.
 * <p>
 * 각 사용자는 하나의 Refresh Token만 가지도록 설계되었으며,
 * userId를 PK로 사용하여 토큰을 식별합니다.
 * <p>
 * 로그아웃 시 해당 토큰은 삭제되며, 이후 동일한 토큰으로는 재발급이 불가능합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code RefreshToken(Long userId, String token, LocalDateTime expiredAt)} <br>
 * 사용자 식별자와 Refresh Token 값, 만료 시간을 기반으로 엔티티를 생성합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data JPA (jakarta.persistence)를 사용하여 영속성을 관리합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
@Entity
@Getter
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    private Long userId;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Builder
    public RefreshToken(Long userId, String token, LocalDateTime expiredAt) {
        this.userId = userId;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public void updateToken(String token, LocalDateTime expiredAt) {
        this.token = token;
        this.expiredAt = expiredAt;
    }
}
