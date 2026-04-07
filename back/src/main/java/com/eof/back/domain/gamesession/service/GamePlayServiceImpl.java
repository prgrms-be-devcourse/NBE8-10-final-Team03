package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameMessageResponse;
import com.eof.back.domain.gamesession.dto.QuizResultResponse;
import com.eof.back.domain.gamesession.dto.QuizResultResponse.PlayerScore;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quiz.dto.QuizBroadcastResponse;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.entity.AnswerType;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.user.gamerecord.dto.GameResultRequest;
import com.eof.back.domain.user.gamerecord.service.RecordService;
import com.eof.back.global.exception.errorCode.GameSessionErrorCode;
import com.eof.back.global.exception.exceptions.GameSessionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamePlayServiceImpl implements GamePlayService {

    private static final int GAME_START_DELAY_SEC = 5;
    private static final int QUIZ_TIME_LIMIT_SEC = 10;
    private static final int GRADING_TIME_SEC = 3;
    private static final int RESULT_SHOW_TIME_SEC = 5;
    private static final int ROUND_INTERVAL_SEC = QUIZ_TIME_LIMIT_SEC + GRADING_TIME_SEC + RESULT_SHOW_TIME_SEC;
    private static final Duration REDIS_KEY_TTL = Duration.ofHours(1);

    private static final String MODEL_V2 = "gemini-embedding-2-preview";
    private static final String MODEL_V1 = "gemini-embedding-001";
    private static final double SIMILARITY_THRESHOLD = 0.85;

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ThreadPoolTaskScheduler gameTaskScheduler;
    private final GameSessionRepository gameSessionRepository;
    private final RecordService recordService;
    private final com.eof.back.global.gemini.GeminiClient geminiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 게임 방마다 실행 중인 스케줄러를 보관 (방 폭파나 게임 종료 시 멈추기 위함)
    private final Map<Long, ScheduledFuture<?>> roomTimers = new ConcurrentHashMap<>();

    /**
     * 공통 유틸리티 메서드: Redis Key 생성기
     */
    private String getRedisKey(Long gameSessionId, String key) {
        return "room:" + gameSessionId + ":" + key;
    }

    /**
     * 공통 유틸리티 메서드: 웹소켓 메시지 브로드캐스트
     */
    private void broadcastToRoom(Long gameSessionId, GameMessageResponse<?> message) {
        messagingTemplate.convertAndSend("/topic/rooms/" + gameSessionId + "/chat", message);
    }

    /**
     * 게임 시작 및 스케줄러 가동
     */
    @Override
    @Transactional
    public void startGame(Long gameSessionId) {

        GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new GameSessionException(GameSessionErrorCode.GAME_SESSION_NOT_FOUND, "방을 찾을 수 없습니다."));

        String hostUsername = gameSession.getHost().getNickname();
        int maxQuizzes = gameSession.getMaxQuizzes();

        String stateKey = getRedisKey(gameSessionId, "status");
        String roundKey = getRedisKey(gameSessionId, "round");
        String maxRoundKey = getRedisKey(gameSessionId, "max_round");
        String questionsKey = getRedisKey(gameSessionId, "questions");
        String scoresKey = getRedisKey(gameSessionId, "scores");
        String userMappingKey = getRedisKey(gameSessionId, "user_mapping");

        // 1. Redis 게임 상태 초기화
        redisTemplate.opsForValue().set(stateKey, "PLAYING");
        redisTemplate.opsForValue().set(roundKey, "0");
        redisTemplate.opsForValue().set(maxRoundKey, String.valueOf(maxQuizzes));

        // 게임 스코어보드 초기화 및 닉네임-ID 매핑 저장
        gameSession.getPlayers().forEach(player -> {
            redisTemplate.opsForZSet().add(scoresKey, player.getNickname(), 0.0);
            // Redis에 저장 (nickname -> userId)
            redisTemplate.opsForHash().put(userMappingKey, player.getNickname(), String.valueOf(player.getId()));
        });
        redisTemplate.expire(scoresKey, REDIS_KEY_TTL);

        // 2. 문제 캐싱 (Preload)
        try {
            redisTemplate.delete(questionsKey);

            // 원본 퀴즈 리스트를 새로운 ArrayList로 복사 후 순서를 무작위로 섞음
            List<Quiz> shuffledQuizzes = new ArrayList<>(gameSession.getQuizSet().getQuizzes());
            Collections.shuffle(shuffledQuizzes);

            int count = 0;
            for (Quiz quiz : shuffledQuizzes) {
                if (count >= maxQuizzes) break;

                QuizResponse quizDto = QuizResponse.from(quiz);
                redisTemplate.opsForList().rightPush(questionsKey, objectMapper.writeValueAsString(quizDto));
                count++;
            }
            redisTemplate.expire(questionsKey, REDIS_KEY_TTL);
        } catch (Exception e) {
            log.error("방 {} - 퀴즈 세팅 중 오류 발생: ", gameSessionId, e);
            throw new RuntimeException("게임 준비 중 오류가 발생했습니다.");
        }

        // 3. 시작 알림
        broadcastToRoom(gameSessionId, GameMessageResponse.enter("SYSTEM", "게임이 시작되었습니다! 잠시 후 첫 번째 문제가 출제됩니다.", null));

        // 게임 세션 상태 시작으로 변경
        gameSession.startGame();
        //  기존 타이머가 있다면 스케줄러 취소 처리
        ScheduledFuture<?> existingTimer = roomTimers.get(gameSessionId);
        if (existingTimer != null) {
            existingTimer.cancel(false);
            log.warn("방 {} 에 이미 실행 중인 스케줄러가 있어 강제 종료 후 재시작합니다.", gameSessionId);
        }
        // 4. 타이머 가동
        roomTimers.put(gameSessionId, gameTaskScheduler.scheduleWithFixedDelay(
                () -> processNextRound(gameSessionId),
                Instant.now().plusSeconds(GAME_START_DELAY_SEC),
                Duration.ofSeconds(ROUND_INTERVAL_SEC)
        ));

        log.info("방 {} 의 게임 스케줄러 가동 완료. (호스트: {}, 문제 수: {})", gameSessionId, hostUsername, maxQuizzes);
    }

    /**
     * 스케줄러 끄기 및 Redis 청소
     */
    @Override
    public void stopGameTimer(Long gameSessionId) {
        ScheduledFuture<?> timer = roomTimers.remove(gameSessionId);
        if (timer != null) {
            timer.cancel(false);
            log.info("방 {} 의 게임 스케줄러가 종료되었습니다.", gameSessionId);
        }

        // Redis 키 삭제
        List<String> keysToDelete = Stream.of(
                "status", "round", "max_round", "questions", "current_answer", "current_answer_type", "answers", "scores", "user_mapping"
        ).map(suffix -> getRedisKey(gameSessionId, suffix)).collect(Collectors.toList());
        redisTemplate.delete(keysToDelete);
    }

    /**
     * 15초마다 스케줄러에 의해 실행되는 문제 출제 로직
     */
    private void processNextRound(Long gameSessionId) {
        try {
            String roundKey = getRedisKey(gameSessionId, "round");
            String maxRoundKey = getRedisKey(gameSessionId, "max_round");
            String questionsKey = getRedisKey(gameSessionId, "questions");

            String currentRoundStr = redisTemplate.opsForValue().get(roundKey);
            int currentRound = currentRoundStr != null ? Integer.parseInt(currentRoundStr) : 0;

            String maxRoundStr = redisTemplate.opsForValue().get(maxRoundKey);
            int maxRound = maxRoundStr != null ? Integer.parseInt(maxRoundStr) : 1;

            if (currentRound >= maxRound) {
                log.info("방 {} 의 모든 문제({}개)가 출제되어 게임이 종료됩니다.", gameSessionId, maxRound);

                // 게임 결과 저장
                endGame(gameSessionId);

                broadcastToRoom(gameSessionId, GameMessageResponse.quizEnd());
                stopGameTimer(gameSessionId);
                return;
            }

            int nextRound = currentRound + 1;
            redisTemplate.opsForValue().set(roundKey, String.valueOf(nextRound));

            String quizJson = redisTemplate.opsForList().leftPop(questionsKey);

            if (quizJson != null) {
                QuizResponse quiz = objectMapper.readValue(quizJson, QuizResponse.class);

                String correctAnswer = quiz.answer();
                AnswerType answerType = quiz.answerType();
                redisTemplate.opsForValue().set(getRedisKey(gameSessionId, "current_answer"), correctAnswer, Duration.ofSeconds(ROUND_INTERVAL_SEC));
                redisTemplate.opsForValue().set(getRedisKey(gameSessionId, "current_answer_type"), answerType.name(), Duration.ofSeconds(ROUND_INTERVAL_SEC));

                // Step 0: 주관식(SHORT_ANSWER)인 경우에만 정답 임베딩 미리 추출
                if (answerType == AnswerType.SHORT_ANSWER) {
                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            List<Double> v2Embedding = geminiClient.embed(correctAnswer, MODEL_V2);
                            List<Double> v1Embedding = geminiClient.embed(correctAnswer, MODEL_V1);
                            redisTemplate.opsForValue().set(getRedisKey(gameSessionId, "current_answer_v2"), objectMapper.writeValueAsString(v2Embedding), Duration.ofSeconds(ROUND_INTERVAL_SEC));
                            redisTemplate.opsForValue().set(getRedisKey(gameSessionId, "current_answer_v1"), objectMapper.writeValueAsString(v1Embedding), Duration.ofSeconds(ROUND_INTERVAL_SEC));
                        } catch (Exception e) {
                            log.error("방 {} - 정답 임베딩 캐싱 중 오류 발생: ", gameSessionId, e);
                        }
                    });
                }

                QuizBroadcastResponse safeQuizData = QuizBroadcastResponse.from(quiz, QUIZ_TIME_LIMIT_SEC);
                broadcastToRoom(gameSessionId, GameMessageResponse.quiz(safeQuizData));

                log.info("방 {} - {} 라운드 문제 출제 완료", gameSessionId, nextRound);

                gameTaskScheduler.schedule(() -> {
                    try {
                        gradeRound(gameSessionId);
                    } catch (Exception e) {
                        log.error("방 {} - 채점 진행 중 내부 오류 발생: ", gameSessionId, e);
                    }
                }, Instant.now().plusSeconds(QUIZ_TIME_LIMIT_SEC));

            } else {
                log.warn("방 {} - Redis에 꺼낼 문제가 없습니다!", gameSessionId);
                stopGameTimer(gameSessionId);
            }

        } catch (Exception e) {
            // Redis 통신 실패 등 예상치 못한 모든 예외 처리
            log.error("방 {} - 다음 라운드 진행 중 치명적인 시스템 오류 발생: ", gameSessionId, e);

            broadcastToRoom(gameSessionId, GameMessageResponse.error(
                    "서버 통신 오류가 발생하여 게임이 강제 종료됩니다."
            ));

            // 스케줄러가 망가진 상태로 방치되지 않도록 안전하게 청소 및 종료
            stopGameTimer(gameSessionId);
        }
    }

    @Override
    public void submitAnswer(Long gameSessionId, String nickname, String answer) {
        String answersKey = getRedisKey(gameSessionId, "answers");
        // 1. 키가 이미 존재하는지 확인
        Boolean hasKey = redisTemplate.hasKey(answersKey);

        // 2. 정답 저장
        redisTemplate.opsForHash().put(answersKey, nickname, answer);

        //  3. 키가 없었던 경우에만 TTL을 설정
        if (Boolean.FALSE.equals(hasKey)) {
            redisTemplate.expire(answersKey, REDIS_KEY_TTL);
        }
        log.info("방 {} - [{}] 님의 정답 제출: {}", gameSessionId, nickname, answer);
    }

    private void endGame(Long gameSessionId) {
        String scoresKey = getRedisKey(gameSessionId, "scores");
        String userMappingKey = getRedisKey(gameSessionId, "user_mapping");

        // 1. Redis에서 최종 점수 랭킹 조회 (내림차순)
        Set<ZSetOperations.TypedTuple<String>> scoreTuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(scoresKey, 0, -1);

        if (scoreTuples == null || scoreTuples.isEmpty()) {
            log.warn("방 {} - 저장할 게임 결과(점수)가 없습니다.", gameSessionId);
            return;
        }

        // 2. Redis에서 닉네임 -> userId 매핑 정보 통째로 가져오기 (DB 조회 X)
        Map<Object, Object> userMapping = redisTemplate.opsForHash().entries(userMappingKey);

        List<GameResultRequest.PlayerResult> playerResults = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : scoreTuples) {
            String nickname = tuple.getValue();
            int score = tuple.getScore() != null ? tuple.getScore().intValue() : 0;

            // 3. 매핑 정보에서 userId 추출
            Object userIdObj = userMapping.get(nickname);

            if (userIdObj != null) {
                Long userId = Long.valueOf(userIdObj.toString());
                playerResults.add(new GameResultRequest.PlayerResult(userId, rank++, score));
            } else {
                log.warn("방 {} - 결과 저장 중 유저 매핑 실패 (닉네임: {})", gameSessionId, nickname);
            }
        }

        // 4. 게임 결과 일괄 저장 (recordService 내부에서 @Transactional 시작됨)
        if (!playerResults.isEmpty()) {
            GameResultRequest gameResultRequest = new GameResultRequest(gameSessionId, playerResults);
            recordService.saveGameResult(gameResultRequest);
            log.info("방 {} - 게임 결과 저장 완료", gameSessionId);
        }
    }

    private void gradeRound(Long gameSessionId) {
        String answerKey = getRedisKey(gameSessionId, "current_answer");
        String typeKey = getRedisKey(gameSessionId, "current_answer_type");
        String answersKey = getRedisKey(gameSessionId, "answers");
        String scoresKey = getRedisKey(gameSessionId, "scores");

        // 1. 서버의 실제 정답
        String correctAnswer = redisTemplate.opsForValue().get(answerKey);
        String answerTypeStr = redisTemplate.opsForValue().get(typeKey);
        if (correctAnswer == null || answerTypeStr == null) return;

        AnswerType answerType = AnswerType.valueOf(answerTypeStr);

        // 2. 유저들이 제출한 정답
        Map<Object, Object> submittedAnswers = redisTemplate.opsForHash().entries(answersKey);
        List<String> correctNickNames = Collections.synchronizedList(new ArrayList<>());

        // 주관식인 경우에만 정답 임베딩 로드 (Step 0에서 저장됨)
        List<Double> answerV2 = (answerType == AnswerType.SHORT_ANSWER) ? loadEmbedding(getRedisKey(gameSessionId, "current_answer_v2")) : null;
        List<Double> answerV1 = (answerType == AnswerType.SHORT_ANSWER) ? loadEmbedding(getRedisKey(gameSessionId, "current_answer_v1")) : null;

        // 정규화된 정답
        String normalizedCorrect = normalize(correctAnswer);

        // 3. 계층적 병렬 채점 로직
        List<java.util.concurrent.CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : submittedAnswers.entrySet()) {
            String nickname = (String) entry.getKey();
            String userAnswer = (String) entry.getValue();

            // Step 1: 정규화 비교 (공백 제거, 소문자 변환)
            if (normalizedCorrect.equals(normalize(userAnswer))) {
                correctNickNames.add(nickname);
                continue;
            }

            // Step 2 & 3: 주관식일 때만 AI 병렬 처리 및 계층적 폴백
            if (answerType == AnswerType.SHORT_ANSWER && (answerV2 != null || answerV1 != null)) {
                futures.add(java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        List<Double> userEmbedding = null;
                        double similarity = 0.0;

                        // Step 3: v2 시도 -> 실패 시 v1 폴백
                        try {
                            userEmbedding = geminiClient.embed(userAnswer, MODEL_V2);
                            if (answerV2 != null) similarity = com.eof.back.global.gemini.GeminiClient.calculateCosineSimilarity(answerV2, userEmbedding);
                        } catch (Exception e) {
                            log.warn("Step 3 - v2 임베딩 실패, v1으로 폴백: {}", userAnswer);
                            userEmbedding = geminiClient.embed(userAnswer, MODEL_V1);
                            if (answerV1 != null) similarity = com.eof.back.global.gemini.GeminiClient.calculateCosineSimilarity(answerV1, userEmbedding);
                        }

                        // Step 4: 판정 (유사도 0.85 이상)
                        if (similarity >= SIMILARITY_THRESHOLD) {
                            correctNickNames.add(nickname);
                        }
                    } catch (Exception e) {
                        log.error("AI 채점 중 오류 발생 ({}): ", nickname, e);
                    }
                }));
            }
        }

        // 모든 AI 채점이 끝날 때까지 대기 (최대 GRADING_TIME_SEC)
        if (!futures.isEmpty()) {
            try {
                java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0]))
                        .get(GRADING_TIME_SEC, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("일부 AI 채점이 타임아웃되었습니다.");
            }
        }

        // 4. 점수 업데이트 및 랭킹 조회
        for (String nickname : correctNickNames) {
            redisTemplate.opsForZSet().incrementScore(scoresKey, nickname, 1.0);
        }

        Set<ZSetOperations.TypedTuple<String>> scoreTuples = redisTemplate.opsForZSet().reverseRangeWithScores(scoresKey, 0, -1);
        List<PlayerScore> scoreboard = new ArrayList<>();
        if (scoreTuples != null) {
            for (ZSetOperations.TypedTuple<String> tuple : scoreTuples) {
                scoreboard.add(new PlayerScore(tuple.getValue(), tuple.getScore().intValue()));
            }
        }

        // 5. 결과 발송
        QuizResultResponse resultData = new QuizResultResponse(correctAnswer, new ArrayList<>(correctNickNames), scoreboard);
        broadcastToRoom(gameSessionId, GameMessageResponse.result(resultData));

        log.info("방 {} - 채점 완료. 정답: {}, 정답자: {}", gameSessionId, correctAnswer, correctNickNames);

        // 6. 청소
        redisTemplate.delete(answersKey);
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.trim().replaceAll("\\\\s+", "").toLowerCase();
    }

    private List<Double> loadEmbedding(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Double>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}