package com.eof.back.domain.user.user.entity;

import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>서비스 이용자의 핵심 도메인 모델입니다.</p>
 * 사용자의 계정 정보, 인증 정보(비밀번호), 프로필 정보(닉네임) 및
 * 게임 플레이를 통해 누적된 전체 랭킹 점수를 관리합니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    /**
     * 사용자의 고유 로그인 아이디.
     * 시스템 내에서 중복될 수 없습니다.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 사용자의 암호화된 비밀번호.
     * 실제 평문 비밀번호가 아닌 해시된 값이 저장되어야 합니다.
     */
    @Column(nullable = false)
    private String password;

    /**
     * 서비스 내에서 표시될 사용자의 별명.
     * 시스템 내에서 중복될 수 없습니다.
     */
    @Column(nullable = false, unique = true)
    private String nickname;

    /**
     * 사용자, 관리자를 구분하는 권한.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * 모든 게임 세션을 통틀어 누적된 전체 랭킹 점수.
     * 기본값은 0이며, 게임 결과에 따라 가산됩니다.
     */
    @Column(nullable = false)
    private Long totalRankingScore = 0L;

    /**
     * 사용자가 탈퇴한 일시.
     * null이면 활성 사용자, 값이 있으면 탈퇴한 사용자입니다.
     */
    @Column
    private LocalDateTime deletedAt;

    /**
     * 게임 결과에 따른 랭킹 점수를 누적 합산합니다.
     *
     * @param score 가산할 랭킹 점수
     */
    public void addRankingScore(Long score) {
        this.totalRankingScore += score;
    }

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param username 사용자 아이디
     * @param password 암호화된 비밀번호
     * @param nickname 사용자 닉네임
     */
    @Builder
    private User(String username, String password, String nickname, Role role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.totalRankingScore = 0L;
    }

    /**
     * User 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param username 사용자 아이디
     * @param password 암호화된 비밀번호
     * @param nickname 사용자 닉네임
     * @return 생성된 User 엔티티 객체
     */
    public static User of(String username, String password, String nickname) {
        return User.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .role(Role.USER)
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
