package com.eof.back.domain.questionreport.entity;

/**
 * <p>문제 신고에 대한 처리 단계(Status)를 정의하는 열거형입니다.</p>
 * 운영진이나 관리자에 의해 신고가 검토되고 해결되었는지를 관리합니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
public enum QuestionReportStatus {
    /**
     * 신고가 접수되었으나 아직 검토가 시작되지 않은 초기 상태입니다.
     */
    PENDING,

    /**
     * 운영진이 해당 문제를 검토하고, 수정이나 삭제 등의 조치가 완료된 상태입니다.
     */
    PROCESSED
}
