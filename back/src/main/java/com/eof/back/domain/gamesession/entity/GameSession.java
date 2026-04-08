package com.eof.back.domain.gamesession.entity;

import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.global.exception.errorCode.GameSessionErrorCode;
import com.eof.back.global.exception.exceptions.GameSessionException;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

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
     * 현재 게임 방의 최대 플레이어수
     * 최소 2명이상
     */
    @Column(nullable = false)
    private Integer maxPlayers;

    /**
     * 현재 게임 방의 플레이어 수
     */
    @Column(nullable = false)
    private Integer currentPlayersCount;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id")
    private List<User> players = new ArrayList<>();

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param roomName   방 제목
     * @param host       방장
     * @param quizSet    퀴즈 세트
     * @param maxQuizzes 최대 퀴즈 수
     * @param maxPlayers 최대 플레이어 수
     * @param status     게임 상태
     */
    @Builder
    private GameSession(String roomName, User host, QuizSet quizSet, Integer maxQuizzes, Integer maxPlayers, GameSessionStatus status) {
        this.roomName = roomName;
        this.host = host;
        this.quizSet = quizSet;
        this.maxQuizzes = maxQuizzes;
        this.maxPlayers = maxPlayers;

        // 방이 처음 생성될 때는 무조건 카운트를 0으로 초기화하고, 명단은 비워둡니다.
        this.currentPlayersCount = 0;
        this.players = new ArrayList<>();
        this.status = status != null ? status : GameSessionStatus.WAIT;
    }

    /**
     * GameSession 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     * 생성 시 상태는 자동으로 WAIT으로 설정됩니다.
     *
     * @param roomName   방 제목
     * @param host       방장 (User 엔티티)
     * @param quizSet    선택된 퀴즈 세트 (QuizSet 엔티티)
     * @param maxQuizzes 세션 내 최대 퀴즈 수
     * @return 생성된 GameSession 엔티티 객체
     */
    public static GameSession of(String roomName, User host, QuizSet quizSet, Integer maxQuizzes, Integer maxPlayers) {
        GameSession gameSession = GameSession.builder()
                .roomName(roomName)
                .host(host)
                .quizSet(quizSet)
                .maxQuizzes(maxQuizzes)
                .maxPlayers(maxPlayers)
                .status(GameSessionStatus.WAIT)
                .build();

        gameSession.join(host);

        return gameSession;
    }

    /**
     * 게임 세션에 참여할때 카운트를 증가시켜주고 List<User> players 리스트에 추가합니다.
     *
     * @param user
     */

    public void join(User user) {
        if (this.currentPlayersCount >= this.maxPlayers) {
            throw new GameSessionException(GameSessionErrorCode.PLAYERS_COUNT_EXCEEDED, "방이 가득 찼습니다.");
        }
        if (!this.players.contains(user)) {
            this.players.add(user);
            this.currentPlayersCount++;
        }
    }

    /**
     * 게임 세션에 나갈 때 카운트를 감소시켜주고 List<User> players 리스트에 제거합니다.
     *
     * @param user
     */
    public void leave(User user) {
        if (this.players.remove(user)) {
            this.currentPlayersCount--;
        }
    }

    public void startGame() {
        this.status = GameSessionStatus.START;
    }

    public void endGame() {
        this.status = GameSessionStatus.END;
    }

    /**
     * 방의 설정을 변경합니다. (방 이름, 퀴즈 세트, 최대 문제 수, 최대 인원수)
     * 대기 중인 상태에서만 변경 가능하며, 방장 권한 체크는 서비스 계층에서 수행합니다.
     */
    public void updateRoom(String roomName, QuizSet quizSet, Integer maxQuizzes, Integer maxPlayers) {
        if (this.status != GameSessionStatus.WAIT) {
            throw new GameSessionException(GameSessionErrorCode.INVALID_GAME_STATUS, "대기 중인 방만 수정할 수 있습니다.");
        }

        if (roomName != null && !roomName.isBlank()) {
            this.roomName = roomName;
        }

        if (quizSet != null) {
            this.quizSet = quizSet;
        }

        // maxQuizzes가 변경되었거나, QuizSet이 변경된 경우 검증
        if (maxQuizzes != null) {
            if (maxQuizzes > this.quizSet.getTotalQuizCount()) {
                throw new GameSessionException(GameSessionErrorCode.MAX_QUIZZES_EXCEEDED, "최대 문제 수는 퀴즈 세트의 총 문제 수(" + this.quizSet.getTotalQuizCount() + "개)보다 많을 수 없습니다.");
            }
            this.maxQuizzes = maxQuizzes;
        } else if (quizSet != null && this.maxQuizzes > quizSet.getTotalQuizCount()) {
            // 퀴즈 세트만 바뀌었는데 기존 maxQuizzes가 더 큰 경우, 퀴즈 세트의 전체 개수로 강제 조정
            this.maxQuizzes = quizSet.getTotalQuizCount();
        }

        if (maxPlayers != null) {
            if (maxPlayers < 2) {
                throw new GameSessionException(GameSessionErrorCode.INVALID_MAX_PLAYERS, "최대 인원수는 최소 2명 이상이어야 합니다.");
            }
            if (maxPlayers < this.currentPlayersCount) {
                throw new GameSessionException(GameSessionErrorCode.PLAYERS_COUNT_EXCEEDED, "현재 참여 중인 인원(" + this.currentPlayersCount + "명)보다 적게 설정할 수 없습니다.");
            }
            this.maxPlayers = maxPlayers;
        }
    }

    /**
     * 게임 세션의 상태를 변경합니다.
     * @param status 변경할 상태
     */
    public void updateStatus(GameSessionStatus status) {
        this.status = status;
    }
}
