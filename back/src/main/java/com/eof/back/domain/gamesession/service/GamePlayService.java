package com.eof.back.domain.gamesession.service;

/**
 * 웹소켓을 통한 실시간 퀴즈 게임의 진행 상태와 스케줄링을 관리하는 서비스 클래스입니다.
 * <p>
 * 방장이 게임을 시작하면 Redis에 게임 상태(현재 라운드, 최대 라운드 등)를 초기화하고,
 * ThreadPoolTaskScheduler를 이용해 독립적인 타이머를 가동합니다.
 * 타이머는 일정 주기마다 다음 라운드 출제 로직을 실행하며, 최대 라운드에 도달하거나
 * 방이 삭제될 경우 스케줄러와 Redis 데이터를 정리하고 종료합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service} 어노테이션을 통해 스프링 빈으로 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 상태 관리를 위해 {@code StringRedisTemplate}(Redis)을 사용하며,
 * 비동기 타이머 처리를 위해 {@code ThreadPoolTaskScheduler}를 사용합니다.
 *
 * @author 유재원
 * @since 2026-03-25
 */
public interface GamePlayService {

    /**
     * 방장이 게임을 시작하고 문제 출제 스케줄러(타이머)를 가동합니다.
     *
     * @param gameSessionId 게임이 시작될 세션의 아이디
     */
    void startGame(Long gameSessionId);

    /**
     * 실행 중인 게임 스케줄러를 종료하고 관련된 임시 데이터(Redis)를 정리합니다.
     *
     * @param gameSessionId 종료할 게임 세션의 아이디
     */
    void stopGameTimer(Long gameSessionId);

    /**
     * 유저가 제출한 퀴즈 정답을 저장소에 임시로 기록합니다.
     *
     * @param gameSessionId 정답을 제출한 게임 세션의 아이디
     * @param username      정답을 제출한 유저의 닉네임
     * @param answer        유저가 제출한 정답 내용
     */
    void submitAnswer(Long gameSessionId, String username, String answer);
}