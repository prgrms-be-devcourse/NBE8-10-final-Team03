package com.eof.back.domain.user.gamerecord.entity;

import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>게임이 종료된 후 참여자 개개인의 성과를 기록하는 엔티티입니다.</p>
 * 특정 게임 세션에서 사용자가 획득한 점수, 최종 순위, 그리고
 * 이 결과가 사용자의 전체 랭킹 시스템에 반영된 점수를 영구적으로 보관합니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
@Entity
@Table(name = "game_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRecord extends BaseEntity {

    /**
     * 게임에 참여한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 성적이 발생한 해당 게임 세션 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    /**
     * 해당 세션 내에서 획득한 순수 게임 점수
     */
    @Column(nullable = false)
    private Integer sessionScore;

    /**
     * 세션 종료 시의 최종 참여자 순위 (1위부터 시작)
     */
    @Column(nullable = false)
    private Integer sessionRanking;

    /**
     * 해당 게임의 점수와 순위를 바탕으로 계산되어 사용자의 전체 랭킹에 가산된 최종 점수
     */
    @Column(nullable = false)
    private Long earnedRankingScore;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param user 참여자
     * @param gameSession 세션
     * @param sessionScore 게임 점수
     * @param sessionRanking 순위
     * @param earnedRankingScore 반영 랭킹 점수
     */
    @Builder
    private GameRecord(User user, GameSession gameSession, Integer sessionScore, Integer sessionRanking, Long earnedRankingScore) {
        this.user = user;
        this.gameSession = gameSession;
        this.sessionScore = sessionScore;
        this.sessionRanking = sessionRanking;
        this.earnedRankingScore = earnedRankingScore;
    }

    /**
     * GameRecord 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param user 참여한 사용자 (User 엔티티)
     * @param gameSession 참여한 세션 (GameSession 엔티티)
     * @param sessionScore 획득 점수
     * @param sessionRanking 최종 순위
     * @param earnedRankingScore 가산될 랭킹 점수
     * @return 생성된 GameRecord 엔티티 객체
     */
    public static GameRecord of(User user, GameSession gameSession, Integer sessionScore, Integer sessionRanking, Long earnedRankingScore) {
        return GameRecord.builder()
                .user(user)
                .gameSession(gameSession)
                .sessionScore(sessionScore)
                .sessionRanking(sessionRanking)
                .earnedRankingScore(earnedRankingScore)
                .build();
    }
}
