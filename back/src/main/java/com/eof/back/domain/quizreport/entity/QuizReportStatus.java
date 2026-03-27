package com.eof.back.domain.quizreport.entity;

/**
 * <p>퀴즈 신고에 대한 처리 단계(Status)를 정의하는 열거형입니다.</p>
 * <p>
 * 신고가 접수되었는지, 검토가 완료되었는지를 구분합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link Enum}을 상속받습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 없음
 *
 * @author MintyU
 * @since 2026-03-18
 */
public enum QuizReportStatus {
    /**
     * 신고가 접수되었으나 아직 검토가 시작되지 않은 초기 상태입니다.
     */
    PENDING,

    /**
     * 운영진이 해당 퀴즈를 검토하고, 수정이나 삭제 등의 조치가 완료된 상태입니다.
     */
    PROCESSED
}
