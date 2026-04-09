package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameMessageResponse;
import com.eof.back.domain.gamesession.entity.GameSession;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

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

    @Test
    @DisplayName("게임 시작 성공 테스트")
    void startGame_Success() {
        // given
        Long gameSessionId = 1L;
        prepareRedisMocks();
        
        User host = mock(User.class);
        given(host.getNickname()).willReturn("hostUser");
        given(host.getId()).willReturn(1L);

        Quiz quiz = mock(Quiz.class);
        given(quiz.getContent()).willReturn("문제");
        given(quiz.getAnswer()).willReturn("정답");
        given(quiz.getAnswerType()).willReturn(AnswerType.SHORT_ANSWER);
        given(quiz.getQuestionType()).willReturn(QuestionType.TEXT);

        QuizSet quizSet = mock(QuizSet.class);
        given(quizSet.getQuizzes()).willReturn(List.of(quiz));

        GameSession gameSession = mock(GameSession.class);
        given(gameSession.getHost()).willReturn(host);
        given(gameSession.getMaxQuizzes()).willReturn(5);
        given(gameSession.getPlayers()).willReturn(List.of(host));
        given(gameSession.getQuizSet()).willReturn(quizSet);

        given(gameSessionRepository.findById(gameSessionId)).willReturn(Optional.of(gameSession));
        
        ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
        doReturn(scheduledFuture).when(gameTaskScheduler).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));

        // when
        gamePlayService.startGame(gameSessionId);

        // then
        verify(valueOperations).set(eq("room:1:status"), eq("PLAYING"));
        verify(gameTaskScheduler).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
    }

    @Test
    @DisplayName("processNextRound 테스트 - 다음 라운드 진행")
    void processNextRound_Success() throws Exception {
        // given
        Long gameSessionId = 1L;
        prepareRedisMocks();
        
        given(valueOperations.get("room:1:round")).willReturn("0");
        given(valueOperations.get("room:1:max_round")).willReturn("5");
        
        QuizResponse quizResponse = QuizResponse.builder()
                .id(1L).content("질문").answer("정답").answerType(AnswerType.SHORT_ANSWER).questionType(QuestionType.TEXT).build();
        given(listOperations.leftPop("room:1:questions")).willReturn(objectMapper.writeValueAsString(quizResponse));

        ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(scheduledFuture).when(gameTaskScheduler).scheduleWithFixedDelay(runnableCaptor.capture(), any(Instant.class), any(Duration.class));
        
        // Setup startGame requirements
        User host = mock(User.class);
        given(host.getNickname()).willReturn("hostUser");
        GameSession gameSession = mock(GameSession.class);
        given(gameSession.getHost()).willReturn(host);
        given(gameSession.getQuizSet()).willReturn(mock(QuizSet.class));
        given(gameSessionRepository.findById(gameSessionId)).willReturn(Optional.of(gameSession));

        gamePlayService.startGame(gameSessionId);
        
        Runnable processNextRoundRunnable = runnableCaptor.getValue();

        // when
        processNextRoundRunnable.run();

        // then
        verify(valueOperations).set(eq("room:1:round"), eq("1"));
        verify(gameTaskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    @DisplayName("gradeRound 테스트 - 정답 채점")
    void gradeRound_Success() throws Exception {
        // given
        Long gameSessionId = 1L;
        prepareRedisMocks();
        
        given(valueOperations.get("room:1:current_answer")).willReturn("정답");
        given(valueOperations.get("room:1:current_answer_type")).willReturn("SHORT_ANSWER");
        given(hashOperations.entries("room:1:answers")).willReturn(Collections.singletonMap("user1", "정답"));

        ArgumentCaptor<Runnable> gradeRoundCaptor = ArgumentCaptor.forClass(Runnable.class);
        
        processNextRound_Success(); 
        
        verify(gameTaskScheduler).schedule(gradeRoundCaptor.capture(), any(Instant.class));
        Runnable gradeRoundRunnable = gradeRoundCaptor.getValue();

        // when
        gradeRoundRunnable.run();

        // then
        verify(zSetOperations).incrementScore(eq("room:1:scores"), eq("user1"), eq(1.0));
    }

    @Test
    @DisplayName("endGame 테스트")
    void endGame_Success() {
        Long gameSessionId = 1L;
        prepareRedisMocks();
        
        ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
        given(tuple.getValue()).willReturn("user1");
        given(tuple.getScore()).willReturn(10.0);
        given(zSetOperations.reverseRangeWithScores("room:1:scores", 0, -1)).willReturn(Collections.singleton(tuple));
        given(hashOperations.entries("room:1:user_mapping")).willReturn(Collections.singletonMap("user1", "101"));
        
        GameSession gameSession = mock(GameSession.class);
        given(gameSessionRepository.findById(gameSessionId)).willReturn(Optional.of(gameSession));

        gamePlayService.endGame(gameSessionId);

        verify(recordService).saveGameResult(any());
    }
}
