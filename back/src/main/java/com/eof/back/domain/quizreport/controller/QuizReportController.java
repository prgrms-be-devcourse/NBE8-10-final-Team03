package com.eof.back.domain.quizreport.controller;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import com.eof.back.domain.quizreport.service.QuizReportService;
import com.eof.back.global.jwt.UserPrincipal;
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
 * 퀴즈 신고(QuizReport)와 관련된 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 사용자가 특정 퀴즈 세트에 대해 문제를 제기(신고)하거나,
 * 관리자가 이를 조회하고 처리 상태를 변경할 수 있는 API를 제공합니다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@code QuizReportController(QuizReportService quizReportService)} <br>
 * 필드 주입을 위한 생성자로, Lombok의 {@link RequiredArgsConstructor}에 의해 자동 생성됩니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * Spring Boot의 {@link RestController}에 의해 Bean으로 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Web, Spring Security, Validation API 등을 사용합니다.
 *
 * @author MintyU
 * @since 2026-03-27
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class QuizReportController {

    private final QuizReportService quizReportService;

    /**
     * 새로운 퀴즈 신고를 등록합니다.
     *
     * @param request   신고 정보 (퀴즈 세트 ID, 사유 등)
     * @param principal 로그인한 사용자 정보 (신고자)
     * @return 생성된 신고의 상세 정보를 포함한 성공 응답 (201 Created)
     */
    @PostMapping
    public ResponseEntity<Response<Long>> createReport(
            @RequestBody @Valid QuizReportCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long reportId = quizReportService.createReport(request, principal.id());
        return ResponseEntity.created(URI.create("/api/v1/reports/" + reportId))
                .body(CommonResponse.success(reportId, "신고가 성공적으로 접수되었습니다."));
    }

    /**
     * 특정 신고 내역의 상세 정보를 조회합니다.
     *
     * @param id        조회할 신고의 식별자
     * @param principal 로그인한 사용자 정보 (관리자 권한 확인용)
     * @return 신고 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response<QuizReportResponse>> getReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        QuizReportResponse response = quizReportService.getReport(id, principal.id());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 모든 퀴즈 신고 내역을 조회합니다. (주로 관리자용)
     *
     * @param principal 로그인한 사용자 정보 (관리자 권한 확인용)
     * @return 전체 신고 목록
     */
    @GetMapping
    public ResponseEntity<Response<List<QuizReportResponse>>> getAllReports(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<QuizReportResponse> response = quizReportService.getAllReports(principal.id());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 특정 신고를 '처리 완료' 상태로 업데이트합니다.
     *
     * @param id        처리할 신고의 식별자
     * @param principal 로그인한 사용자 정보 (관리자 권한 확인용)
     * @return 성공 메시지 응답
     */
    @PatchMapping("/{id}/process")
    public ResponseEntity<Response<Void>> processReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        quizReportService.processReport(id, principal.id());
        return ResponseEntity.ok(CommonResponse.success(null, "신고 처리가 완료되었습니다."));
    }

    /**
     * 특정 신고 내역을 삭제합니다.
     *
     * @param id        삭제할 신고의 식별자
     * @param principal 로그인한 사용자 정보 (관리자 권한 확인용)
     * @return 204 No Content 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        quizReportService.deleteReport(id, principal.id());
        return ResponseEntity.noContent().build();
    }
}
