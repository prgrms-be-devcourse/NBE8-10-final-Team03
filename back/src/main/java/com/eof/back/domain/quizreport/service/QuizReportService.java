package com.eof.back.domain.quizreport.service;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import java.util.List;

/**
 * 퀴즈 신고(QuizReport)와 관련된 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 사용자의 신고 접수, 관리자의 신고 내역 조회 및 상태 변경 등
 * 퀴즈 신고 도메인의 핵심 기능을 명세합니다.
 * </p>
 *
 * <p><b>빈 관리:</b><br>
 * 인터페이스 자체는 빈으로 관리되지 않으며, 구현체인 {@link QuizReportServiceImpl}이 빈으로 관리됩니다.
 *
 * @author MintyU
 * @since 2026-03-27
 */
public interface QuizReportService {

    /**
     * 새로운 퀴즈 신고를 생성합니다.
     *
     * @param request 신고 정보가 담긴 DTO
     * @param userId 신고를 수행하는 사용자의 ID
     * @return 생성된 신고의 식별자
     */
    Long createReport(QuizReportCreateRequest request, Long userId);

    /**
     * 특정 신고 내역의 상세 정보를 조회합니다.
     *
     * @param id 조회할 신고의 식별자
     * @param userId 요청을 수행하는 사용자의 ID (관리자 권한 확인용)
     * @return 신고 상세 정보 DTO
     */
    QuizReportResponse getReport(Long id, Long userId);

    /**
     * 모든 퀴즈 신고 목록을 조회합니다.
     *
     * @param userId 요청을 수행하는 사용자의 ID (관리자 권한 확인용)
     * @return 신고 목록 DTO 리스트
     */
    List<QuizReportResponse> getAllReports(Long userId);

    /**
     * 신고 상태를 '처리 완료(PROCESSED)'로 업데이트합니다.
     *
     * @param id 처리할 신고의 식별자
     * @param userId 요청을 수행하는 사용자의 ID (관리자 권한 확인용)
     */
    void processReport(Long id, Long userId);

    /**
     * 신고 내역을 삭제합니다.
     *
     * @param id 삭제할 신고의 식별자
     * @param userId 요청을 수행하는 사용자의 ID (관리자 권한 확인용)
     */
    void deleteReport(Long id, Long userId);
}
