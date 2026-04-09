package com.eof.back.domain.gamesession.service;

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
import com.eof.back.global.exception.exceptions.GameSessionException;
import com.eof.back.global.gemini.GeminiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GamePlayServiceImplTest {

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
    }

    private void prepareRedisMocks() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("gradeRound 브랜치 - 정답 정보 부재 시 조기 종료")
    void gradeRound_EarlyReturn_NoAnswerInfo() {
        Long sessionId = 1L;
        prepareRedisMocks();
        given(valueOperations.get("room:1:current_answer")).willReturn(null);

        ReflectionTestUtils.invokeMethod(gamePlayService, "gradeRound", sessionId);

        verify(hashOperations, never()).entries(anyString());
    }

    @Test
    @DisplayName("gradeRound 브랜치 - 객관식 문제 정답/오답 처리")
    void gradeRound_MultipleChoice() {
        Long sessionId = 1L;
        prepareRedisMocks();
        given(valueOperations.get("room:1:current_answer")).willReturn("1");
        given(valueOperations.get("room:1:current_answer_type")).willReturn("MULTIPLE_CHOICE");
        given(hashOperations.entries("room:1:answers")).willReturn(Map.of(
                "user1", "1",
                "user2", "2"
        ));

        ReflectionTestUtils.invokeMethod(gamePlayService, "gradeRound", sessionId);

        verify(zSetOperations).incrementScore(anyString(), eq("user1"), eq(1.0));
        verify(zSetOperations, never()).incrementScore(anyString(), eq("user2"), anyDouble());
    }

    @Test
    @DisplayName("gradeRound 브랜치 - 주관식 완벽 일치 (AI 호출 없음)")
    void gradeRound_ShortAnswer_ExactMatch() {
        Long sessionId = 1L;
        prepareRedisMocks();
        given(valueOperations.get("room:1:current_answer")).willReturn("치킨");
        given(valueOperations.get("room:1:current_answer_type")).willReturn("SHORT_ANSWER");
        given(hashOperations.entries("room:1:answers")).willReturn(Map.of("user1", "치킨"));

        ReflectionTestUtils.invokeMethod(gamePlayService, "gradeRound", sessionId);

        verify(zSetOperations).incrementScore(anyString(), eq("user1"), eq(1.0));
        verify(geminiClient, never()).embed(anyString(), anyString());
    }

    @Test
    @DisplayName("gradeRound 브랜치 - 주관식 AI 유사도 통과 (v2 기반)")
    void gradeRound_ShortAnswer_AiSuccessV2() throws Exception {
        Long sessionId = 1L;
        prepareRedisMocks();
        given(valueOperations.get("room:1:current_answer")).willReturn("강아지");
        given(valueOperations.get("room:1:current_answer_type")).willReturn("SHORT_ANSWER");
        given(hashOperations.entries("room:1:answers")).willReturn(Map.of("user1", "멍멍이"));

        List<Double> answerEmb = List.of(1.0, 0.0);
        List<Double> userEmb = List.of(0.9, 0.1); 
        given(valueOperations.get("room:1:current_answer_v2")).willReturn(objectMapper.writeValueAsString(answerEmb));
        given(geminiClient.embed("멍멍이", "gemini-embedding-2-preview")).willReturn(userEmb);

        ReflectionTestUtils.invokeMethod(gamePlayService, "gradeRound", sessionId);

        verify(zSetOperations).incrementScore(anyString(), eq("user1"), eq(1.0));
    }

    @Test
    @DisplayName("gradeRound 브랜치 - 모든 임베딩 실패 시 오답 처리")
    void gradeRound_ShortAnswer_AllAiFail() throws Exception {
        Long sessionId = 1L;
        prepareRedisMocks();
        given(valueOperations.get("room:1:current_answer")).willReturn("물");
        given(valueOperations.get("room:1:current_answer_type")).willReturn("SHORT_ANSWER");
        given(hashOperations.entries("room:1:answers")).willReturn(Map.of("user1", "H2O"));

        given(valueOperations.get(anyString())).willReturn(null);
        given(geminiClient.embed(anyString(), anyString())).willReturn(null);

        ReflectionTestUtils.invokeMethod(gamePlayService, "gradeRound", sessionId);

        verify(zSetOperations, never()).incrementScore(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("processNextRound 브랜치 테스트 - 다음 라운드 진행 (퀴즈 데이터가 있을 때)")
    void processNextRound_Success() throws Exception {
        Long sessionId = 1L;
        prepareRedisMocks();
        given(valueOperations.get("room:1:round")).willReturn("0");
        given(valueOperations.get("room:1:max_round")).willReturn("5");
        
        QuizResponse quizResponse = QuizResponse.builder()
                .id(1L).content("질문").answer("정답").answerType(AnswerType.SHORT_ANSWER).questionType(QuestionType.TEXT).build();
        given(listOperations.leftPop("room:1:questions")).willReturn(objectMapper.writeValueAsString(quizResponse));

        ReflectionTestUtils.invokeMethod(gamePlayService, "processNextRound", sessionId);

        verify(valueOperations).set(eq("room:1:round"), eq("1"));
        verify(valueOperations).set(eq("room:1:current_answer"), eq("정답"), any(Duration.class));
    }

    @Test
    @DisplayName("endGame 상세 검증 - 점수 집계, 유저 매핑, 결과 저장 및 상태 변경 확인")
    void endGame_Success_FullValidation() {
        Long sessionId = 1L;
        prepareRedisMocks();

        ZSetOperations.TypedTuple<String> user1Score = mock(ZSetOperations.TypedTuple.class);
        when(user1Score.getValue()).thenReturn("user1");
        when(user1Score.getScore()).thenReturn(10.0);

        ZSetOperations.TypedTuple<String> user2Score = mock(ZSetOperations.TypedTuple.class);
        when(user2Score.getValue()).thenReturn("user2");
        when(user2Score.getScore()).thenReturn(5.0);

        Set<ZSetOperations.TypedTuple<String>> scoreTuples = new LinkedHashSet<>(List.of(user1Score, user2Score));
        given(zSetOperations.reverseRangeWithScores("room:1:scores", 0, -1)).willReturn(scoreTuples);
        given(hashOperations.entries("room:1:user_mapping")).willReturn(Map.of("user1", "101", "user2", "102"));

        GameSession session = mock(GameSession.class);
        QuizSet quizSet = mock(QuizSet.class);
        given(session.getQuizSet()).willReturn(quizSet);
        given(gameSessionRepository.findById(sessionId)).willReturn(Optional.of(session));

        gamePlayService.endGame(sessionId);

        ArgumentCaptor<com.eof.back.domain.user.gamerecord.dto.GameResultRequest> requestCaptor = 
                ArgumentCaptor.forClass(com.eof.back.domain.user.gamerecord.dto.GameResultRequest.class);
        verify(recordService).saveGameResult(requestCaptor.capture());
        
        assertThat(requestCaptor.getValue().sessionId()).isEqualTo(1L);
        assertThat(requestCaptor.getValue().playerResults()).hasSize(2);
        verify(session).updateStatus(GameSessionStatus.WAIT);
    }

    @Test
    @DisplayName("stopGameTimer 테스트 - 타이머 취소 및 Redis 리소스 정리 확인")
    void stopGameTimer_Success() {
        Long sessionId = 1L;
        ScheduledFuture scheduledFuture = mock(ScheduledFuture.class);
        Map<Long, ScheduledFuture<?>> roomTimers = (Map<Long, ScheduledFuture<?>>) ReflectionTestUtils.getField(gamePlayService, "roomTimers");
        roomTimers.put(sessionId, scheduledFuture);

        gamePlayService.stopGameTimer(sessionId);

        verify(scheduledFuture).cancel(false);
        verify(redisTemplate).delete(anyList());
        assertThat(roomTimers).doesNotContainKey(sessionId);
    }

    @Test
    @DisplayName("submitAnswer 테스트 - 첫 번째 제출 시 TTL 설정 확인")
    void submitAnswer_FirstSubmission_SetsTTL() {
        Long sessionId = 1L;
        prepareRedisMocks();
        given(redisTemplate.hasKey("room:1:answers")).willReturn(false);

        gamePlayService.submitAnswer(sessionId, "user1", "정답");

        verify(hashOperations).put(eq("room:1:answers"), eq("user1"), eq("정답"));
        verify(redisTemplate).expire(eq("room:1:answers"), any(Duration.class));
    }

    @Test
    @DisplayName("startGame 실패 - 세션 정보를 찾을 수 없는 경우")
    void startGame_Fail_NotFound() {
        Long sessionId = 999L;
        given(gameSessionRepository.findById(sessionId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> gamePlayService.startGame(sessionId))
                .isInstanceOf(GameSessionException.class);
    }

    @Test
    @DisplayName("startGame 성공 - 기본 흐름 확인")
    void startGame_Success_FullValidation() throws Exception {
        Long sessionId = 1L;
        prepareRedisMocks();
        User host = mock(User.class);
        given(host.getNickname()).willReturn("hostUser");
        given(host.getId()).willReturn(10L);

        Quiz quiz = mock(Quiz.class);
        given(quiz.getContent()).willReturn("문제1");
        given(quiz.getAnswer()).willReturn("정답1");
        given(quiz.getAnswerType()).willReturn(AnswerType.SHORT_ANSWER);
        given(quiz.getQuestionType()).willReturn(QuestionType.TEXT);

        QuizSet quizSet = mock(QuizSet.class);
        given(quizSet.getQuizzes()).willReturn(List.of(quiz));

        GameSession session = mock(GameSession.class);
        given(session.getHost()).willReturn(host);
        given(session.getPlayers()).willReturn(List.of(host));
        given(session.getQuizSet()).willReturn(quizSet);
        given(session.getMaxQuizzes()).willReturn(5);

        given(gameSessionRepository.findById(sessionId)).willReturn(Optional.of(session));
        given(gameTaskScheduler.scheduleWithFixedDelay(any(Runnable.class), any(java.time.Instant.class), any(java.time.Duration.class)))
                .willReturn(mock(ScheduledFuture.class));

        gamePlayService.startGame(sessionId);

        verify(valueOperations).set(eq("room:1:status"), eq("PLAYING"));
        verify(listOperations, atLeastOnce()).rightPush(eq("room:1:questions"), anyString());
        verify(zSetOperations).add(eq("room:1:scores"), eq("hostUser"), eq(0.0));
    }

    @Test
    @DisplayName("startGame 성공 - 기존 타이머 취소 확인")
    void startGame_Success_WithExistingTimer() {
        Long sessionId = 1L;
        prepareRedisMocks();
        ScheduledFuture existingTimer = mock(ScheduledFuture.class);
        Map<Long, ScheduledFuture<?>> roomTimers = (Map<Long, ScheduledFuture<?>>) ReflectionTestUtils.getField(gamePlayService, "roomTimers");
        roomTimers.put(sessionId, existingTimer);

        User host = mock(User.class);
        given(host.getNickname()).willReturn("hostUser");
        GameSession session = mock(GameSession.class);
        given(session.getHost()).willReturn(host);
        given(session.getPlayers()).willReturn(List.of(host));
        given(session.getQuizSet()).willReturn(mock(QuizSet.class));
        given(gameSessionRepository.findById(sessionId)).willReturn(Optional.of(session));
        given(gameTaskScheduler.scheduleWithFixedDelay(any(Runnable.class), any(java.time.Instant.class), any(java.time.Duration.class)))
                .willReturn(mock(ScheduledFuture.class));

        gamePlayService.startGame(sessionId);

        verify(existingTimer).cancel(false);
    }

    @Test
    @DisplayName("startGame 실패 - 퀴즈 캐싱 중 예외 발생")
    void startGame_Fail_QuizCachingError() {
        Long sessionId = 1L;
        prepareRedisMocks();
        GameSession session = mock(GameSession.class);
        User host = mock(User.class);
        given(host.getNickname()).willReturn("host");
        given(session.getHost()).willReturn(host);
        given(session.getPlayers()).willReturn(List.of(host));
        given(gameSessionRepository.findById(sessionId)).willReturn(Optional.of(session));
        
        doThrow(new RuntimeException("Redis error")).when(redisTemplate).delete(anyString());

        assertThatThrownBy(() -> gamePlayService.startGame(sessionId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("게임 준비 중 오류가 발생했습니다.");
    }
}
