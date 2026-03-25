package com.eof.back.domain.quiz.controller;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quiz.service.QuizService;
import com.eof.back.domain.user.dto.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 퀴즈(Quiz)와 관련된 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 특정 퀴즈 세트(QuizSet) 하위의 개별 퀴즈에 대한 생성, 조회, 수정, 삭제(CRUD) 기능을 제공합니다.
 * 모든 요청은 {@code /api/v1/quizsets/{quizSetId}/quizzes} 경로를 기반으로 하며,
 * 보안을 위해 사용자의 인증 정보를 확인하고 데이터의 일관성을 검증합니다.
 * </p>
 *
 * <p><b>빈 관리:</b><br>
 * {@link org.springframework.web.bind.annotation.RestController} 어노테이션을 통해 스프링 MVC 컨트롤러 빈으로 등록됩니다.
 *
 * <p><b>권한 모델:</b><br>
 * - <b>조회:</b> 인증된 모든 사용자가 가능합니다.<br>
 * - <b>생성/수정/삭제:</b> 해당 퀴즈 세트를 생성한 소유자(Creator)만 수행할 수 있습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Security의 {@link AuthenticationPrincipal}을 활용하여 요청자의 식별자를 획득합니다.
 *
 * @author MintyU
 * @since 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/quizsets/{quizSetId}/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 특정 퀴즈 세트에 새로운 퀴즈를 생성하여 추가합니다.
     * <p>
     * 성공적으로 생성되면 HTTP 201 Created 상태 코드와 함께
     * 생성된 리소스의 위치를 가리키는 {@code Location} 헤더를 반환합니다.
     * </p>
     *
     * @param quizSetId 퀴즈를 추가할 대상 퀴즈 세트의 식별자
     * @param request   생성할 퀴즈 정보 (내용, 정답, 선택지 등)
     * @param principal 현재 로그인한 사용자의 정보
     * @return 생성 결과 응답 (HTTP 201 Created)
     */
    @PostMapping
    public ResponseEntity<Void> createQuiz(
            @PathVariable Long quizSetId,
            @RequestBody @Valid QuizCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long quizId = quizService.createQuiz(quizSetId, request, principal.id());

        String location = String.format("/api/v1/quizsets/%d/quizzes/%d", quizSetId, quizId);
        return ResponseEntity.created(URI.create(location)).build();
    }

    /**
     * 특정 퀴즈 세트에 포함된 모든 퀴즈 목록을 조회합니다.
     *
     * @param quizSetId 퀴즈 목록을 조회할 퀴즈 세트의 식별자
     * @return 퀴즈 상세 정보 목록을 담은 성공 응답
     */
    @GetMapping
    public ResponseEntity<Response<List<QuizResponse>>> getQuizzesByQuizSetId(@PathVariable Long quizSetId) {
        List<QuizResponse> response = quizService.getQuizzesByQuizSetId(quizSetId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 특정 퀴즈 세트 내의 특정 퀴즈 상세 정보를 조회합니다.
     *
     * @param quizSetId 퀴즈가 속한 퀴즈 세트의 식별자
     * @param quizId    조회할 퀴즈의 식별자
     * @return 퀴즈 상세 정보를 담은 성공 응답
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<Response<QuizResponse>> getQuiz(
            @PathVariable Long quizSetId,
            @PathVariable Long quizId) {
        QuizResponse response = quizService.getQuiz(quizSetId, quizId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 기존 퀴즈 정보를 수정합니다.
     * <p>
     * 클라이언트로부터 전달받은 필드 정보를 바탕으로 퀴즈 내용을 업데이트합니다.
     * </p>
     *
     * @param quizSetId 퀴즈가 속한 퀴즈 세트의 식별자
     * @param quizId    수정할 퀴즈의 식별자
     * @param request   수정할 퀴즈 정보
     * @param principal 현재 로그인한 사용자의 정보
     * @return 수정된 퀴즈의 식별자를 담은 성공 응답
     */
    @PatchMapping("/{quizId}")
    public ResponseEntity<Response<Long>> updateQuiz(
            @PathVariable Long quizSetId,
            @PathVariable Long quizId,
            @RequestBody @Valid QuizUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long updatedId = quizService.updateQuiz(quizSetId, quizId, request, principal.id());
        return ResponseEntity.ok(CommonResponse.success(updatedId));
    }

    /**
     * 특정 퀴즈를 삭제합니다.
     * <p>
     * 성공 시 HTTP 204 No Content 상태 코드를 반환합니다.
     * </p>
     *
     * @param quizSetId 퀴즈가 속한 퀴즈 세트의 식별자
     * @param quizId    삭제할 퀴즈의 식별자
     * @param principal 현재 로그인한 사용자의 정보
     * @return 삭제 성공 응답 (HTTP 204 No Content)
     */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizSetId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal principal) {
        quizService.deleteQuiz(quizSetId, quizId, principal.id());
        return ResponseEntity.noContent().build();
    }
}
