package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameMessageResponse;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.entity.GameSessionStatus;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.entity.AnswerType;
import com.eof.back.domain.quiz.entity.QuestionType;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.gamerecord.service.RecordService;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.global.gemini.GeminiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GamePlayServiceImplTest {

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
    private RecordService recordService;
    @Mock
    private GeminiClient geminiClient;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ZSetOperations<String, String> zSetOperations;
    @Mock
    private ListOperations<String, String> listOperations;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private GamePlayService self;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        gamePlayService = new GamePlayServiceImpl(
                redisTemplate,
                messagingTemplate,
                gameTaskScheduler,
                gameSessionRepository,
                recordService,
                geminiClient,
                self
        );
    }

    private void prepareRedisMocks() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    private void setupStartGameMock(Long gameSessionId) {
        User host = mock(User.class);
        given(host.getNickname()).willReturn("hostUser");
        GameSession gameSession = mock(GameSession.class);
        given(gameSession.getHost()).willReturn(host);
        given(gameSession.getQuizSet()).willReturn(mock(QuizSet.class));
        given(gameSessionRepository.findById(gameSessionId)).willReturn(Optional.of(gameSession));
    }

    @Test
    @DisplayName("게임 시작 성공 테스트")
    void startGame_Success() {
        Long gameSessionId = 1L;
        prepareRedisMocks();
        setupStartGameMock(gameSessionId);
        
        ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
        given(gameTaskScheduler.scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class)))
                .willReturn(scheduledFuture);

        gamePlayService.startGame(gameSessionId);

        verify(valueOperations).set(eq("room:1:status"), eq("PLAYING"));
    }

    @Test
    @DisplayName("processNextRound 테스트 - 다음 라운드 진행")
    void processNextRound_Success() throws Exception {
        Long gameSessionId = 1L;
        prepareRedisMocks();
        
        given(valueOperations.get("room:1:round")).willReturn("0");
        given(valueOperations.get("room:1:max_round")).willReturn("5");
        
        QuizResponse quizResponse = QuizResponse.builder()
                .id(1L).content("질문").answer("정답").answerType(AnswerType.SHORT_ANSWER).questionType(QuestionType.TEXT).build();
        given(listOperations.leftPop("room:1:questions")).willReturn(objectMapper.writeValueAsString(quizResponse));

        ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        given(gameTaskScheduler.scheduleWithFixedDelay(runnableCaptor.capture(), any(Instant.class), any(Duration.class)))
                .willReturn(scheduledFuture);
        
        setupStartGameMock(gameSessionId);
        gamePlayService.startGame(gameSessionId);
        runnableCaptor.getValue().run();

        verify(valueOperations).set(eq("room:1:round"), eq("1"));
    }

    @Test
    @DisplayName("endGame 테스트 - 결과가 없는 경우")
    void endGame_NoResults() {
        Long gameSessionId = 1L;
        prepareRedisMocks();
        given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).willReturn(null);
        
        GameSession gameSession = mock(GameSession.class);
        given(gameSessionRepository.findById(gameSessionId)).willReturn(Optional.of(gameSession));

        gamePlayService.endGame(gameSessionId);

        verify(gameSession).updateStatus(GameSessionStatus.WAIT);
    }

    @Test
    @DisplayName("submitAnswer 테스트 - 이미 키가 있는 경우")
    void submitAnswer_KeyExists() {
        Long gameSessionId = 1L;
        prepareRedisMocks();
        given(redisTemplate.hasKey(anyString())).willReturn(true);

        gamePlayService.submitAnswer(gameSessionId, "user1", "정답");

        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }
}
