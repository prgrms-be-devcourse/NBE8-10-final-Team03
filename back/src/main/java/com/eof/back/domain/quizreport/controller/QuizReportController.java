package com.eof.back.domain.quizreport.controller;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.service.QuizReportService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 퀴즈 신고(QuizReport)와 관련된 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 일반 사용자가 특정 퀴즈 세트에 대해 문제를 제기(신고)하는 기능을 제공합니다.
 * 관리자용 조회 및 처리 기능은 AdminController로 이관되었습니다.
 * </p>
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
     * @return 생성된 신고의 식별자(ID)를 포함한 성공 응답 (201 Created)
     */
    @PostMapping
    public ResponseEntity<Response<Long>> createReport(
            @RequestBody @Valid QuizReportCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long reportId = quizReportService.createReport(request, principal.id());
        return ResponseEntity.created(URI.create("/api/v1/reports/" + reportId))
                .body(CommonResponse.success(reportId, "신고가 성공적으로 접수되었습니다."));
    }
}
