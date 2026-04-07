package com.eof.back.domain.ai.service;

import com.eof.back.domain.ai.dto.AiQuizGenerateResponse;
import com.eof.back.domain.ai.dto.AiQuizResponse;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import com.eof.back.infrastructure.gemini.GeminiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 퀴즈 생성 기능의 구현체입니다.
 * <p>
 * Gemini API를 호출하여 주제 기반 객관식 퀴즈 5개를 생성하고,
 * {@link com.eof.back.domain.quizset.entity.QuizSet}으로 저장합니다.
 * 부적절한 주제 요청 시 빈 배열을 반환하도록 프롬프트를 설계하였으며,
 * 빈 배열 반환 시 {@link com.eof.back.global.exception.exceptions.QuizSetException}을 발생시킵니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Gemini API ({@code gemini-3.1-flash-lite-preview}) 호출에
 * Spring WebFlux의 {@link org.springframework.web.reactive.function.client.WebClient}를 사용합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service}로 등록되며, 생성자 주입을 통해 의존성을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @see AiQuizService
 * @since 2026-04-02
 */

@Service
@RequiredArgsConstructor
public class AiQuizServiceImpl implements AiQuizService {


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizRepository quizRepository;
    private final GeminiClient geminiClient;


    @Override
    @Transactional
    public AiQuizGenerateResponse generateQuiz(String topic, Long userId) {
        String response = geminiClient.call(topic);
        List<AiQuizResponse> quizzes = parseQuizzes(response);

        if (quizzes.isEmpty()) {
            throw new QuizSetException(QuizSetErrorCode.INVALID_TOPIC);
        }

        Long quizSetId = saveQuizSet(topic, quizzes, userId);
        return new AiQuizGenerateResponse(quizSetId);
    }

    private Long saveQuizSet(String topic, List<AiQuizResponse> quizzes, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        QuizSet quizSet = QuizSet.of("[AI] " + topic, "AI가 생성한 " + topic + " 퀴즈", user);
        quizSetRepository.save(quizSet);

        List<Quiz> quizList = quizzes.stream()
                .map(quiz -> Quiz.builder()
                        .quizSet(quizSet)
                        .content(quiz.content())
                        .answer(quiz.answer())
                        .choice1(quiz.choice1())
                        .choice2(quiz.choice2())
                        .choice3(quiz.choice3())
                        .choice4(quiz.choice4())
                        .build())
                .toList();

        quizRepository.saveAll(quizList);

// increaseQuizCount가 1씩 증가하는 구조라 size만큼 반복
        for (int i = 0; i < quizList.size(); i++) {
            quizSet.increaseQuizCount();
        }
        return quizSet.getId();
    }


    private List<AiQuizResponse> parseQuizzes(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode candidate = root.path("candidates").get(0);
            if (candidate == null) {
                throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_CREATE_FAIL);
            }

            JsonNode part = candidate.path("content").path("parts").get(0);
            if (part == null) {
                throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_CREATE_FAIL);
            }

            String text = part.path("text").asText();
            String json = text
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(json, new TypeReference<List<AiQuizResponse>>() {
            });
        } catch (QuizSetException e) {
            throw e;
        } catch (Exception e) {
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_CREATE_FAIL);
        }
    }
}