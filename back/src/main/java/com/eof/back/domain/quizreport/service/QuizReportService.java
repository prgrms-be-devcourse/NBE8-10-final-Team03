package com.eof.back.domain.quizreport.service;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import java.util.List;

/**
 * 퀴즈 신고(QuizReport) 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 *
 * @author MintyU
 * @since 2026-03-27
 */
public interface QuizReportService {

    /**
     * 새로운 퀴즈 신고를 생성합니다.
     *
     * @param request 신고 정보 (퀴즈 세트 ID, 사유 등)
     * @param userId  신고를 수행하는 사용자의 식별자
     * @return 생성된 신고의 식별자(ID)
     */
    Long createReport(QuizReportCreateRequest request, Long userId);

    // 관리자 전용 기능은 AdminService로 이관됨 (getReport, getAllReports, processReport, deleteReport)
}
