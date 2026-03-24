package com.eof.back.domain.quiz.controller;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.service.QuizService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 퀴즈(Quiz)와 관련된 HTTP 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 특정 퀴즈 세트 내에 속한 퀴즈의 생성 및 조회 API를 제공합니다.
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

        String location = String.format("/api/v1/quizsets/%d/quizzes/%d", quizSetId, quizId);
        return ResponseEntity.created(URI.create(location)).build();
    }

    /**
     * 특정 퀴즈 세트에 포함된 모든 퀴즈 목록을 조회합니다.
     *
     * @param quizSetId 퀴즈 세트의 식별자
     * @return 퀴즈 목록 응답
     */
    @GetMapping
    public ResponseEntity<Response<List<QuizResponse>>> getQuizzesByQuizSetId(@PathVariable Long quizSetId) {
        List<QuizResponse> response = quizService.getQuizzesByQuizSetId(quizSetId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 특정 퀴즈 세트 내의 특정 퀴즈 상세 정보를 조회합니다.
     *
     * @param quizId 조회할 퀴즈의 식별자
     * @return 퀴즈 상세 정보 응답
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<Response<QuizResponse>> getQuiz(
            @PathVariable Long quizSetId, // 경량화를 위해 사용하지 않더라도 경로 변수 일관성 유지
            @PathVariable Long quizId) {
        QuizResponse response = quizService.getQuiz(quizId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
