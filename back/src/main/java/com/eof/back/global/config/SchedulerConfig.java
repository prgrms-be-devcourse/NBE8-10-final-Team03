package com.eof.back.global.config;

/**
 * 실시간 퀴즈 게임의 비동기 타이머 작업을 처리하기 위한 스케줄러 설정 클래스입니다.
 * <p>
 * 다수의 게임 방에서 동시에 실행되는 퀴즈 출제 및 채점 스케줄링 작업을
 * 병렬로 안전하게 처리하기 위해 {@link ThreadPoolTaskScheduler}를 구성합니다.
 * 10개의 스레드를 미리 풀(Pool)에 생성하여 스레드 생성 비용을 줄이고 성능을 최적화합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Configuration}을 통해 스프링 설정 클래스로 동작하며,
 * {@code gameTaskScheduler()}가 반환하는 스케줄러 객체가 스프링 빈(Bean)으로 자동 등록 및 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 비동기 및 스케줄링 작업을 위해 Spring Scheduling ({@code org.springframework.scheduling.concurrent})을 사용합니다.
 *
 * @author 유재원
 * @see com.eof.back.domain.gamesession.service.GamePlayService
 * @since 2026-03-25
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler gameTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 동시 진행되는 방이 많을 것을 대비해 스레드 10개 할당 (필요에 따라 조절)
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("game-scheduler-");
        return scheduler;
    }
}