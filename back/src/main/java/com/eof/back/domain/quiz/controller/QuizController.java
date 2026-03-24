package com.eof.back.domain.quiz.controller;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quiz.service.QuizService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 퀴즈(Quiz)와 관련된 HTTP 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 특정 퀴즈 세트 내에 속한 퀴즈의 생성, 조회, 수정, 삭제 API를 제공합니다.
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
     * @param request   퀴즈 생성 요청 정보
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
            @PathVariable Long quizSetId,
            @PathVariable Long quizId) {
        QuizResponse response = quizService.getQuiz(quizId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 특정 퀴즈 정보를 수정합니다. (일부 필드 수정 가능)
     *
     * @param quizSetId 퀴즈 세트의 식별자
     * @param quizId    수정할 퀴즈의 식별자
     * @param request   수정 요청 정보
     * @return 수정된 퀴즈 정보 응답
     */
    @PatchMapping("/{quizId}")
    public ResponseEntity<Response<Long>> updateQuiz(
            @PathVariable Long quizSetId,
            @PathVariable Long quizId,
            @RequestBody @Valid QuizUpdateRequest request) {
        Long updatedId = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(CommonResponse.success(updatedId));
    }

    /**
     * 특정 퀴즈를 삭제합니다.
     *
     * @param quizSetId 퀴즈 세트의 식별자
     * @param quizId    삭제할 퀴즈의 식별자
     * @return 204 No Content
     */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizSetId,
            @PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }
}
