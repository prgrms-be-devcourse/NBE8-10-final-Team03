package com.eof.back.domain.questionreport.entity;

import com.eof.back.domain.question.entity.Question;
import com.eof.back.domain.user.entity.User;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>문항의 오류나 부적절한 내용을 사용자가 신고했을 때 생성되는 엔티티입니다.</p>
 * 신고자, 대상 문항, 구체적인 신고 사유를 보관하며,
 * 관리자가 이를 검토하고 처리할 수 있도록 상태 정보를 포함합니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
@Entity
@Table(name = "question_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionReport extends BaseEntity {

    /**
     * 해당 문항에 대해 문제를 제기한 사용자 (신고자)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * 신고 대상이 된 구체적인 문항 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 사용자가 작성한 상세 신고 사유 (예: 오타, 중복 정답, 부적절한 언어 등)
     */
    @Column(nullable = false, length = 1000)
    private String reason;

    /**
     * 신고 건에 대한 현재 처리 상태입니다. 초기 생성 시 PENDING으로 설정됩니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionReportStatus status;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param reporter 신고자
     * @param question 대상 문제
     * @param reason 사유
     * @param status 처리 상태
     */
    @Builder
    private QuestionReport(User reporter, Question question, String reason, QuestionReportStatus status) {
        this.reporter = reporter;
        this.question = question;
        this.reason = reason;
        this.status = status != null ? status : QuestionReportStatus.PENDING;
    }

    /**
     * QuestionReport 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param reporter 신고를 수행한 사용자 (User 엔티티)
     * @param question 문제가 제기된 문항 (Question 엔티티)
     * @param reason 신고 사유 텍스트
     * @return 생성된 QuestionReport 엔티티 객체
     */
    public static QuestionReport of(User reporter, Question question, String reason) {
        return QuestionReport.builder()
                .reporter(reporter)
                .question(question)
                .reason(reason)
                .status(QuestionReportStatus.PENDING)
                .build();
    }
}
