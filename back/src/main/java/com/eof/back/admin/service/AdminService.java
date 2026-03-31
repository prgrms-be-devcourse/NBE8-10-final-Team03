package com.eof.back.admin.service;

import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 서비스의 모든 관리자 전용 기능을 정의하는 비즈니스 로직 계층 인터페이스입니다.
 * <p>
 * 관리자 권한(ADMIN)을 가진 사용자만 이 인터페이스의 기능을 호출할 수 있으며,
 * 퀴즈 세트 및 개별 퀴즈에 대한 강제 수정/삭제, 사용자 신고 내역 관리,
 * 비정상 활동 사용자 정지 및 강제 탈퇴 기능을 통합적으로 처리합니다.
 * </p>
 *
 * @author MintyU
 * @since 2026-03-31
 */
public interface AdminService {

    // --- 퀴즈 세트 및 개별 퀴즈 강제 관리 (소유권 무시) ---

    /**
     * 관리자 권한으로 특정 퀴즈 세트의 기본 정보를 강제로 수정합니다.
     * 일반 사용자용 로직과 달리 제작자 본인 여부를 확인하지 않습니다.
     *
     * @param id      수정할 대상 퀴즈 세트의 식별자
     * @param request 수정할 제목 및 설명 정보
     * @param adminId 요청을 보낸 관리자의 식별자 (권한 검증용)
     * @return 수정된 퀴즈 세트의 식별자
     */
    Long updateQuizSet(Long id, QuizSetUpdateRequest request, Long adminId);

    /**
     * 관리자 권한으로 특정 퀴즈 세트를 강제 삭제합니다.
     * 연관된 북마크, 게임 기록, 퀴즈들도 함께 제거됩니다.
     *
     * @param id      삭제할 대상 퀴즈 세트의 식별자
     * @param adminId 요청을 보낸 관리자의 식별자 (권한 검증용)
     */
    void deleteQuizSet(Long id, Long adminId);

    /**
     * 특정 퀴즈 세트 하위의 개별 퀴즈 정보를 강제로 수정합니다.
     *
     * @param quizSetId 퀴즈가 소속된 세트의 식별자
     * @param quizId    수정할 퀴즈의 고유 식별자
     * @param request   수정할 문제 발문 및 보기 정답 정보
     * @param adminId   요청을 보낸 관리자의 식별자 (권한 검증용)
     * @return 수정된 퀴즈의 고유 식별자
     */
    Long updateQuiz(Long quizSetId, Long quizId, QuizUpdateRequest request, Long adminId);

    /**
     * 특정 퀴즈를 강제 삭제합니다.
     * 삭제 시 해당 소속 세트의 전체 퀴즈 카운트가 동기화됩니다.
     *
     * @param quizSetId 퀴즈가 소속된 세트의 식별자
     * @param quizId    삭제할 퀴즈의 고유 식별자
     * @param adminId   요청을 보낸 관리자의 식별자 (권한 검증용)
     */
    void deleteQuiz(Long quizSetId, Long quizId, Long adminId);

    // --- 사용자 신고(Report) 내역 관리 ---

    /**
     * 접수된 특정 신고 내역의 상세 정보를 조회합니다.
     *
     * @param id      조회할 신고 식별자
     * @param adminId 요청을 보낸 관리자의 식별자 (권한 검증용)
     * @return 신고 상세 응답 DTO
     */
    QuizReportResponse getReport(Long id, Long adminId);

    /**
     * 시스템에 접수된 모든 미처리 및 처리 완료 신고 목록을 조회합니다.
     *
     * @param adminId 요청을 보낸 관리자의 식별자 (권한 검증용)
     * @return 전체 신고 목록 리스트
     */
    List<QuizReportResponse> getAllReports(Long adminId);

    /**
     * 특정 신고를 확인하여 '처리 완료' 상태로 전환합니다.
     *
     * @param id      처리할 신고 식별자
     * @param adminId 요청을 보낸 관리자의 식별자 (권한 검증용)
     */
    void processReport(Long id, Long adminId);

    /**
     * 특정 신고 내역을 완전히 삭제합니다.
     *
     * @param id      삭제할 신고 식별자
     * @param adminId 요청을 보낸 관리자의 식별자 (권한 검증용)
     */
    void deleteReport(Long id, Long adminId);

    // --- 사용자 계정 상태 강제 제어 ---

    /**
     * 비정상 활동 사용자를 지정된 기한까지 정지(SUSPENDED) 처리합니다.
     * 정지 기간과 구체적인 사유를 {@link com.eof.back.admin.entity.UserSuspension}에 기록합니다.
     *
     * @param targetUserId 정지 대상이 될 일반 사용자의 식별자
     * @param reason       정지 처분의 사유
     * @param until        정지 상태가 해제될 시각
     * @param adminId      요청을 보낸 관리자의 식별자 (권한 검증용)
     */
    void suspendUser(Long targetUserId, String reason, LocalDateTime until, Long adminId);

    /**
     * 특정 사용자를 시스템에서 강제로 탈퇴(DELETED) 처리합니다.
     * 계정 상태가 DELETED로 변경되며, 연관된 활성 정지 정보도 함께 제거됩니다.
     *
     * @param targetUserId 삭제 처리할 사용자의 식별자
     * @param adminId      요청을 보낸 관리자의 식별자 (권한 검증용)
     */
    void deleteUser(Long targetUserId, Long adminId);
}
