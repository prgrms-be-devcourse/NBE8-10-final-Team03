package com.eof.back.domain.quiz.controller;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;

/**
 * 퀴즈(Quiz)와 관련된 HTTP 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 특정 퀴즈 세트 내에 새로운 퀴즈를 생성하는 등의 API를 제공합니다.
 * </p>
 *
 * @author MintyU
 * @since 2026-03-24
 */
@RestController
@RequestMapping("/api/v1/quizsets/{quizSetId}/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 특정 퀴즈 세트에 새로운 퀴즈를 생성하여 추가합니다.
     *
     * @param quizSetId 퀴즈를 추가할 대상 퀴즈 세트의 식별자
     * @param request   퀴즈 생성 요청 정보 (문제 내용, 정답, 선택지 등)
     * @return 생성 결과 응답 (201 Created)
     */
    @PostMapping
    public ResponseEntity<Void> createQuiz(
            @PathVariable Long quizSetId,
            @RequestBody @Valid QuizCreateRequest request) {

        Long quizId = quizService.createQuiz(quizSetId, request);

        return ResponseEntity.created(URI.create("/api/v1/quizzes/" + quizId))
                .build();
    }
}
