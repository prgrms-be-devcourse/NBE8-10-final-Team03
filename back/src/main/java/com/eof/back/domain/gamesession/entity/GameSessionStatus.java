package com.eof.back.domain.gamesession.entity;

/**
 * <p>게임 세션의 진행 상태를 정의하는 열거형(Enum)입니다.</p>
 * 세션의 생성부터 종료까지의 라이프사이클을 관리하기 위해 사용됩니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
public enum GameSessionStatus {
    /**
     * 방이 생성되어 참여자들을 기다리는 초기 대기 상태입니다.
     */
    WAIT,

    /**
     * 모든 참여자가 준비되었거나 방장에 의해 게임이 공식적으로 시작된 상태입니다.
     */
    START,

    /**
     * 모든 문제가 출제 완료되었거나 세션이 정상적으로 종료된 상태입니다.
     */
    END
}
