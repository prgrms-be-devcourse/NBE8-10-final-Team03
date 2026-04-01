package com.eof.back.domain.quizreport.dto;

import com.eof.back.domain.quizreport.entity.QuizReport;
import com.eof.back.domain.quizreport.entity.QuizReportStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 신고 내역 조회 시 반환되는 데이터 전송 객체입니다.
 * <p>
 * 엔티티 객체를 외부에 노출하지 않기 위해 사용됩니다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@code QuizReportResponse(...)} <br>
 * 빌더 패턴에 의해 사용되는 생성자입니다. <br>
 *
 * @author MintyU
 * @since 2026-03-27
 */
@Getter
@Builder
public class QuizReportResponse {

    private Long id;
    private Long quizSetId;
    private String quizSetTitle;
    private String reporterNickname;
    private String reason;
    private QuizReportStatus status;
    private LocalDateTime createdAt;

    /**
     * QuizReport 엔티티로부터 QuizReportResponse DTO를 생성합니다.
     *
     * @param quizReport 퀴즈 신고 엔티티
     * @return 퀴즈 신고 응답 DTO
     */
    public static QuizReportResponse from(QuizReport quizReport) {
        return QuizReportResponse.builder()
                .id(quizReport.getId())
                .quizSetId(quizReport.getQuizSet().getId())
                .quizSetTitle(quizReport.getQuizSet().getTitle())
                .reporterNickname(quizReport.getReporter().getNickname())
                .reason(quizReport.getReason())
                .status(quizReport.getStatus())
                .createdAt(quizReport.getCreatedAt())
                .build();
    }
}
