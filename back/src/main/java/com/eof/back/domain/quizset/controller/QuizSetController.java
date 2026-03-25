package com.eof.back.domain.quizset.controller;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.service.QuizSetService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 퀴즈 세트(QuizSet)와 관련된 HTTP 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 클라이언트로부터의 퀴즈 세트 생성, 조회, 수정 등의 REST API 요청을 받아 적절한 서비스 메서드를 호출하고 결과를 반환합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link org.springframework.web.bind.annotation.RestController} 어노테이션을 통해 스프링 빈으로 관리됩니다. <br>
 *
 * @author MintyU
 * @since 2026-03-19
 */
@RestController
@RequestMapping("/api/v1/quizsets")
@RequiredArgsConstructor
public class QuizSetController {

    private final QuizSetService quizSetService;

    /**
     * 새로운 퀴즈 세트를 생성합니다.
     *
     * @param request 퀴즈 세트 생성 요청 정보
     * @param principal 로그인한 사용자 정보
     * @return 생성 결과 응답 (201 Created with Location header)
     */
    @PostMapping
    public ResponseEntity<Void> createQuizSet(
            @RequestBody @Valid QuizSetCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long quizSetId = quizSetService.createQuizSet(request, principal.id());
        return ResponseEntity.created(java.net.URI.create("/api/v1/quizsets/" + quizSetId))
                .build();
    }

    /**
     * 특정 퀴즈 세트의 상세 정보를 조회합니다.
     *
     * @param id 조회할 퀴즈 세트의 식별자
     * @return 퀴즈 목록을 포함한 퀴즈 세트 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response<QuizSetResponse>> getQuizSet(@PathVariable Long id) {
        QuizSetResponse response = quizSetService.getQuizSet(id);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 전체 퀴즈 세트 목록을 조회합니다.
     *
     * @return 퀴즈 세트 요약 정보 목록
     */
    @GetMapping
    public ResponseEntity<Response<List<QuizSetListResponse>>> getAllQuizSets() {
        List<QuizSetListResponse> response = quizSetService.getAllQuizSets();
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
