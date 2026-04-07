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
     * 사용자의 이메일 주소.
     * 소셜 로그인 시 제공자로부터 받아옵니다.
     */
    @Column
    private String email;

    /**
     * 사용자의 암호화된 비밀번호.
     * 소셜 로그인 사용자는 비밀번호가 없으므로 nullable입니다.
     */
    @Column
    private String password;

    /**
     * 인증 제공자 (LOCAL, GOOGLE, KAKAO).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    /**
     * 소셜 제공자가 부여한 사용자 고유 ID.
     * LOCAL 로그인 사용자는 null입니다.
     */
    @Column(unique = true)
    private String providerId;

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
     * 사용자 계정 상태.
     * ACTIVE면 정상 활동 중, SUSPENDED면 정지, DELETED면 탈퇴한 사용자입니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 현재 상태로 변경된 시각.
     * 정지 기간 계산, 탈퇴 후 복구 기간 계산 등에 활용됩니다.
     */
    @Column
    private LocalDateTime statusChangedAt;

    /**
     * 프로필 이미지 번호 (1~4).
     * 회원가입 시 랜덤 배정되며, 마이페이지에서 변경 가능합니다.
     */
    @Column(nullable = false)
    private Integer profileImage = 1;

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
     * @param username   사용자 아이디
     * @param email      이메일 주소 (소셜 로그인 시 제공자로부터 수신)
     * @param password   암호화된 비밀번호 (소셜 로그인 사용자는 null)
     * @param nickname   사용자 닉네임
     * @param role       사용자 권한
     * @param provider   인증 제공자 (LOCAL, GOOGLE, KAKAO)
     * @param providerId 소셜 제공자가 부여한 고유 ID (LOCAL 사용자는 null)
     */
    @Builder
    private User(String username, String email, String password, String nickname, Role role, AuthProvider provider, String providerId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.totalRankingScore = 0L;
        this.status = UserStatus.ACTIVE;
        this.profileImage = (int) (Math.random() * 4) + 1;
    }

    /**
     * 일반 회원가입(LOCAL) 유저 생성 팩토리 메서드입니다.
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
                .provider(AuthProvider.LOCAL)
                .build();
    }

    /**
     * 소셜 로그인 유저 생성 팩토리 메서드입니다.
     * username은 "{provider}_{providerId}" 형태로 자동 생성됩니다.
     *
     * @param email      소셜 제공자로부터 받은 이메일 주소
     * @param nickname   소셜 제공자로부터 받은 닉네임
     * @param provider   인증 제공자 (GOOGLE, KAKAO)
     * @param providerId 소셜 제공자가 부여한 고유 ID
     * @return 생성된 User 엔티티 객체
     */
    public static User ofSocial(String email, String nickname, AuthProvider provider, String providerId) {
        return User.builder()
                .username(provider.name() + "_" + providerId)
                .email(email)
                .nickname(nickname)
                .role(Role.USER)
                .provider(provider)
                .providerId(providerId)
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        this.statusChangedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.statusChangedAt = LocalDateTime.now();
    }

    public void delete() {
        this.status = UserStatus.DELETED;
        this.statusChangedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == UserStatus.SUSPENDED;
    }

    public boolean isDeleted() {
        return this.status == UserStatus.DELETED;
    }

    public void updateProfileImage(Integer profileImage) {
        this.profileImage = profileImage;
    }
}
