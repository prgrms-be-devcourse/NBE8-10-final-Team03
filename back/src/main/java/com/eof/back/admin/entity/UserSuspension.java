package com.eof.back.admin.entity;

import com.eof.back.domain.user.user.entity.User;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 특정 사용자의 서비스 이용 정지 상세 정보를 관리하는 엔티티입니다.
 * <p>
 * {@link User} 엔티티와 1:1 관계를 맺으며, 사용자가 정지 상태(SUSPENDED)일 때
 * 해당 정지의 구체적인 사유와 정지 종료 기한을 저장합니다.
 * </p>
 *
 * @author MintyU
 * @since 2026-03-31
 */
@Entity
@Table(name = "user_suspensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSuspension extends BaseEntity {

    /**
     * 정지 처리가 적용된 대상 사용자입니다.
     * 한 명의 사용자는 하나의 활성화된 정지 정보만 가질 수 있습니다.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * 관리자가 입력한 사용자의 정지 사유입니다.
     */
    @Column(nullable = false)
    private String reason;

    /**
     * 사용자의 정지 상태가 유지되는 최종 일시입니다.
     * 이 시각 이후에는 시스템에 의해 혹은 수동으로 정지가 해제될 수 있습니다.
     */
    @Column(nullable = false)
    private LocalDateTime suspendedUntil;

    /**
     * 빌더 패턴을 이용한 내부 생성자입니다.
     *
     * @param user           정지 대상 사용자
     * @param reason         정지 사유
     * @param suspendedUntil 정지 종료 일시
     */
    @Builder
    private UserSuspension(User user, String reason, LocalDateTime suspendedUntil) {
        this.user = user;
        this.reason = reason;
        this.suspendedUntil = suspendedUntil;
    }

    /**
     * 새로운 정지 정보를 생성하는 정적 팩토리 메서드입니다.
     *
     * @param user           정지 대상 사용자
     * @param reason         정지 사유
     * @param suspendedUntil 정지 종료 일시
     * @return 생성된 UserSuspension 인스턴스
     */
    public static UserSuspension of(User user, String reason, LocalDateTime suspendedUntil) {
        return UserSuspension.builder()
                .user(user)
                .reason(reason)
                .suspendedUntil(suspendedUntil)
                .build();
    }

    /**
     * 기존 정지 정보를 새로운 사유와 기한으로 업데이트합니다.
     * 주로 정지 기간 연장이나 사유 정정에 사용됩니다.
     *
     * @param reason         새로운 정지 사유
     * @param suspendedUntil 새로운 정지 종료 일시
     */
    public void update(String reason, LocalDateTime suspendedUntil) {
        if (reason != null) this.reason = reason;
        if (suspendedUntil != null) this.suspendedUntil = suspendedUntil;
    }
}
