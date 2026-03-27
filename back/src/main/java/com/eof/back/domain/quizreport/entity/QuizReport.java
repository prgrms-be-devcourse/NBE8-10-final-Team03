package com.eof.back.domain.quizreport.entity;

import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.user.entity.User;
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
 * <p>퀴즈의 오류나 부적절한 내용을 사용자가 신고했을 때 생성되는 핵심 도메인 모델입니다.</p>
 * 신고자, 대상 퀴즈셋, 구체적인 신고 사유를 보관하며, 관리자의 검토 상태를 관리합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseEntity}를 상속받아 생성/수정 시간을 관리합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code QuizReport(User reporter, QuizSet quizSet, String reason, QuizReportStatus status)} <br>
 * 빌더 패턴을 사용하여 내부에서 호출되는 생성자입니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * JPA Entity로 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * JPA(Jakarta Persistence API)를 사용합니다.
 *
 * @author MintyU
 * @since 2026-03-18
 */
@Entity
@Table(name = "quiz_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizReport extends BaseEntity {

    /**
     * 해당 퀴즈에 대해 문제를 제기한 사용자 (신고자)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * 신고 대상이 된 구체적인 퀴즈셋 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

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
    private QuizReportStatus status;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param reporter 신고자
     * @param quizSet 대상 퀴즈셋
     * @param reason 사유
     * @param status 처리 상태
     */
    @Builder
    private QuizReport(User reporter, QuizSet quizSet, String reason, QuizReportStatus status) {
        this.reporter = reporter;
        this.quizSet = quizSet;
        this.reason = reason;
        this.status = status != null ? status : QuizReportStatus.PENDING;
    }

    /**
     * QuizReport 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param reporter 신고를 수행한 사용자 (User 엔티티)
     * @param quizSet 문제가 제기된 퀴즈셋 (QuizSet 엔티티)
     * @param reason 신고 사유 텍스트
     * @return 생성된 QuizReport 엔티티 객체
     */
    public static QuizReport of(User reporter, QuizSet quizSet, String reason) {
        return QuizReport.builder()
                .reporter(reporter)
                .quizSet(quizSet)
                .reason(reason)
                .status(QuizReportStatus.PENDING)
                .build();
    }

    /**
     * 신고 처리 상태를 완료(PROCESSED)로 변경합니다.
     */
    public void process() {
        this.status = QuizReportStatus.PROCESSED;
    }
}
