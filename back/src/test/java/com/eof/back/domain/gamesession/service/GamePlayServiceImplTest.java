package com.eof.back.domain.gamesession.service;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author 유재원
 * @see
 * @since 2026-03-27
 */

import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.gamerecord.service.RecordService;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.global.exception.exceptions.GameSessionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamePlayServiceImplTest {

    @InjectMocks
    private GamePlayServiceImpl gamePlayService;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ThreadPoolTaskScheduler gameTaskScheduler;
    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ListOperations<String, String> listOperations;
    @Mock
    private ZSetOperations<String, String> zSetOperations;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ScheduledFuture scheduledFuture;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("게임 시작 성공 - Redis 초기화 및 스케줄러 가동")
    void startGame_Success() {
        Long sessionId = 1L;

        User host = mock(User.class);
        given(host.getNickname()).willReturn("hostUser");
        given(host.getId()).willReturn(10L);

        User player2 = mock(User.class);
        given(player2.getNickname()).willReturn("player2");
        given(player2.getId()).willReturn(20L);

        Quiz mockQuiz = mock(Quiz.class);

        QuizSet quizSet = mock(QuizSet.class);
        given(quizSet.getQuizzes()).willReturn(List.of(mockQuiz));

        GameSession mockSession = mock(GameSession.class);
        given(mockSession.getHost()).willReturn(host);
        given(mockSession.getMaxQuizzes()).willReturn(5);
        given(mockSession.getPlayers()).willReturn(List.of(host, player2));
        given(mockSession.getQuizSet()).willReturn(quizSet);

        given(gameSessionRepository.findById(sessionId)).willReturn(Optional.of(mockSession));

        given(gameTaskScheduler.scheduleWithFixedDelay(any(Runnable.class), any(), any(Duration.class)))
                .willReturn(scheduledFuture);

        gamePlayService.startGame(sessionId);

        verify(valueOperations).set("room:1:status", "PLAYING");
        verify(valueOperations).set("room:1:round", "0");
        verify(valueOperations).set("room:1:max_round", "5");

        // 유저 매핑 및 점수판이 잘 세팅되었는지 검증
        verify(zSetOperations).add("room:1:scores", "hostUser", 0.0);
        verify(hashOperations).put("room:1:user_mapping", "hostUser", "10");
        verify(zSetOperations).add("room:1:scores", "player2", 0.0);
        verify(hashOperations).put("room:1:user_mapping", "player2", "20");

        // 스케줄러가 호출되었는지 검증
        verify(gameTaskScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), any(), any(Duration.class));

        // 웹소켓 브로드캐스트가 발생했는지 검증
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/rooms/1/chat"), any(Object.class));
    }

    @Test
    @DisplayName("게임 시작 실패 - 방을 찾을 수 없음")
    void startGame_Fail_SessionNotFound() {
        Long sessionId = 999L;
        given(gameSessionRepository.findById(sessionId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> gamePlayService.startGame(sessionId))
                .isInstanceOf(GameSessionException.class);
    }

    @Test
    @DisplayName("정답 제출 성공 - Redis에 저장 및 TTL 설정")
    void submitAnswer_Success() {
        Long sessionId = 1L;
        String nickname = "testUser";
        String answer = "정답입니다";
        String answersKey = "room:1:answers";

        given(redisTemplate.hasKey(answersKey)).willReturn(false);

        gamePlayService.submitAnswer(sessionId, nickname, answer);

        verify(hashOperations).put(answersKey, nickname, answer);
        verify(redisTemplate).expire(eq(answersKey), any(Duration.class));
    }

    @Test
    @DisplayName("게임 타이머 정지 및 Redis 캐시 청소 성공")
    void stopGameTimer_Success() {
        Long sessionId = 1L;

        gamePlayService.stopGameTimer(sessionId);

        verify(redisTemplate, times(1)).delete(any(List.class));
    }
}