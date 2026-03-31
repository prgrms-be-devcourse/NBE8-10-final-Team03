package com.eof.back.admin.controller;

import com.eof.back.admin.service.AdminService;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서비스 관리자 전용 기능을 제공하는 REST 컨트롤러입니다.
 * <p>
 * 모든 엔드포인트는 {@code /api/v1/admin}으로 시작하며,
 * Spring Security를 통해 {@code ROLE_ADMIN} 권한을 가진 사용자만 접근할 수 있도록 보호됩니다.
 * </p>
 *
 * @author MintyU
 * @since 2026-03-31
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // --- 퀴즈 및 퀴즈 세트 강제 관리 ---

    /**
     * 특정 퀴즈 세트의 정보를 강제로 수정합니다.
     *
     * @param id        수정할 퀴즈 세트 ID
     * @param request   수정할 데이터 (제목, 설명)
     * @param principal 인증된 관리자 정보
     * @return 수정된 퀴즈 세트 ID를 포함한 응답
     */
    @PatchMapping("/quizsets/{id}")
    public ResponseEntity<Response<Long>> updateQuizSet(
            @PathVariable Long id,
            @RequestBody @Valid QuizSetUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long updatedId = adminService.updateQuizSet(id, request, principal.id());
        return ResponseEntity.ok(CommonResponse.success(updatedId, "퀴즈 세트가 수정되었습니다."));
    }

    /**
     * 특정 퀴즈 세트를 강제로 삭제합니다.
     *
     * @param id        삭제할 퀴즈 세트 ID
     * @param principal 인증된 관리자 정보
     * @return 204 No Content
     */
    @DeleteMapping("/quizsets/{id}")
    public ResponseEntity<Void> deleteQuizSet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.deleteQuizSet(id, principal.id());
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 퀴즈의 내용을 강제로 수정합니다.
     *
     * @param quizSetId 퀴즈가 소속된 세트 ID
     * @param quizId    수정할 퀴즈 ID
     * @param request   수정할 데이터
     * @param principal 인증된 관리자 정보
     * @return 수정된 퀴즈 ID를 포함한 응답
     */
    @PatchMapping("/quizsets/{quizSetId}/quizzes/{quizId}")
    public ResponseEntity<Response<Long>> updateQuiz(
            @PathVariable Long quizSetId,
            @PathVariable Long quizId,
            @RequestBody @Valid QuizUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long updatedId = adminService.updateQuiz(quizSetId, quizId, request, principal.id());
        return ResponseEntity.ok(CommonResponse.success(updatedId, "퀴즈가 수정되었습니다."));
    }

    /**
     * 특정 퀴즈를 강제로 삭제합니다.
     *
     * @param quizSetId 퀴즈가 소속된 세트 ID
     * @param quizId    삭제할 퀴즈 ID
     * @param principal 인증된 관리자 정보
     * @return 204 No Content
     */
    @DeleteMapping("/quizsets/{quizSetId}/quizzes/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizSetId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.deleteQuiz(quizSetId, quizId, principal.id());
        return ResponseEntity.noContent().build();
    }

    // --- 사용자 신고 내역 관리 ---

    /**
     * 시스템 전체 신고 내역 목록을 조회합니다.
     *
     * @param principal 인증된 관리자 정보
     * @return 전체 신고 목록
     */
    @GetMapping("/reports")
    public ResponseEntity<Response<List<QuizReportResponse>>> getAllReports(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<QuizReportResponse> reports = adminService.getAllReports(principal.id());
        return ResponseEntity.ok(CommonResponse.success(reports));
    }

    /**
     * 특정 신고 내역의 상세 정보를 조회합니다.
     *
     * @param id        신고 식별자
     * @param principal 인증된 관리자 정보
     * @return 신고 상세 정보
     */
    @GetMapping("/reports/{id}")
    public ResponseEntity<Response<QuizReportResponse>> getReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        QuizReportResponse report = adminService.getReport(id, principal.id());
        return ResponseEntity.ok(CommonResponse.success(report));
    }

    /**
     * 특정 신고를 '처리 완료' 상태로 변경합니다.
     *
     * @param id        처리할 신고 식별자
     * @param principal 인증된 관리자 정보
     * @return 성공 메시지 응답
     */
    @PatchMapping("/reports/{id}/process")
    public ResponseEntity<Response<Void>> processReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.processReport(id, principal.id());
        return ResponseEntity.ok(CommonResponse.success(null, "신고 처리가 완료되었습니다."));
    }

    /**
     * 특정 신고 내역을 삭제합니다.
     *
     * @param id        삭제할 신고 식별자
     * @param principal 인증된 관리자 정보
     * @return 204 No Content
     */
    @DeleteMapping("/reports/{id}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.deleteReport(id, principal.id());
        return ResponseEntity.noContent().build();
    }

    // --- 사용자 계정 및 상태 관리 ---

    /**
     * 특정 사용자를 지정된 사유와 기간(일 단위)으로 서비스 이용 정지 처리합니다.
     *
     * @param userId    정지 대상 사용자 ID
     * @param request   정지 사유 및 일수 정보
     * @param principal 인증된 관리자 정보
     * @return 성공 메시지 응답
     */
    @PatchMapping("/users/{userId}/suspend")
    public ResponseEntity<Response<Void>> suspendUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserSuspensionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.suspendUser(userId, request.reason(), request.suspensionDays(), principal.id());
        return ResponseEntity.ok(CommonResponse.success(null, "사용자가 정지되었습니다."));
    }

    /**
     * 특정 사용자를 강제로 탈퇴(상태 변경) 처리합니다.
     *
     * @param userId    삭제 대상 사용자 ID
     * @param principal 인증된 관리자 정보
     * @return 성공 메시지 응답
     */
    @PatchMapping("/users/{userId}/delete")
    public ResponseEntity<Response<Void>> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        adminService.deleteUser(userId, principal.id());
        return ResponseEntity.ok(CommonResponse.success(null, "사용자가 삭제(탈퇴) 처리되었습니다."));
    }

    /**
     * 사용자 정지 처리를 위한 요청 데이터 전송 객체입니다.
     *
     * @param reason         정지 사유
     * @param suspensionDays 정지 기간 (일 단위, 최소 1일)
     */
    public record UserSuspensionRequest(
            @NotBlank(message = "정지 사유는 필수입니다.")
            String reason,

            @NotNull(message = "정지 기간은 필수입니다.")
            @Min(value = 1, message = "정지 기간은 최소 1일 이상이어야 합니다.")
            Integer suspensionDays
    ) {}
}
