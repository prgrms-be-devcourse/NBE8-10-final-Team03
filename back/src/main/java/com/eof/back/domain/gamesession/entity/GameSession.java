package com.eof.back.domain.gamesession.entity;

import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.entity.User;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>실시간 게임이 진행되는 방(세션)의 설정과 상태를 관리하는 엔티티입니다.</p>
 * 방장 정보, 사용할 퀴즈 세트, 최대 퀴즈 수 등을 설정하고
 * 현재 세션이 대기 중인지, 진행 중인지, 종료되었는지를 실시간으로 추적합니다.
 *
 * @author MintyU
 * @since 2026-03-18
 */
@Entity
@Table(name = "game_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameSession extends BaseEntity {

    /**
     * 로비나 목록에서 표시될 게임 방의 제목
     */
    @Column(nullable = false)
    private String roomName;

    /**
     * 해당 게임 방을 개설하고 관리 권한을 가진 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    /**
     * 이 세션에서 풀게 될 퀴즈 세트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

    /**
     * 한 세션 동안 출제할 총 퀴즈 수입니다.
     * 퀴즈 세트의 전체 퀴즈 수보다 작거나 같을 수 있습니다.
     */
    @Column(nullable = false)
    private Integer maxQuizzes;

    /**
     * 현재 게임 방의 진행 상태 (WAIT, START, END)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameSessionStatus status;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param roomName 방 제목
     * @param host 방장
     * @param quizSet 퀴즈 세트
     * @param maxQuizzes 최대 퀴즈 수
     * @param status 게임 상태
     */
    @Builder
    private GameSession(String roomName, User host, QuizSet quizSet, Integer maxQuizzes, GameSessionStatus status) {
        this.roomName = roomName;
        this.host = host;
        this.quizSet = quizSet;
        this.maxQuizzes = maxQuizzes;
        this.status = status != null ? status : GameSessionStatus.WAIT;
    }

    /**
     * GameSession 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     * 생성 시 상태는 자동으로 WAIT으로 설정됩니다.
     *
     * @param roomName 방 제목
     * @param host 방장 (User 엔티티)
     * @param quizSet 선택된 퀴즈 세트 (QuizSet 엔티티)
     * @param maxQuizzes 세션 내 최대 퀴즈 수
     * @return 생성된 GameSession 엔티티 객체
     */
    public static GameSession of(String roomName, User host, QuizSet quizSet, Integer maxQuizzes) {
        return GameSession.builder()
                .roomName(roomName)
                .host(host)
                .quizSet(quizSet)
                .maxQuizzes(maxQuizzes)
                .status(GameSessionStatus.WAIT)
                .build();
    }
}
