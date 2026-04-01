package com.eof.back.global.exception.errorCode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 퀴즈 신고 관리에서 발생하는 예외에 대한 상수 값을 정의합니다.
 * <p>
 * {@link ErrorCode} 인터페이스를 구현하며, HTTP 상태 코드와 메시지를 관리합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode} 인터페이스를 구현합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code QuizReportErrorCode(HttpStatus status, String message)} <br>
 * 열거형의 상태와 메시지를 설정하는 생성자입니다. <br>
 *
 * @author MintyU
 * @since 2026-03-27
 */
@AllArgsConstructor
public enum QuizReportErrorCode implements ErrorCode {
    QUIZ_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 신고 내역을 찾을 수 없습니다."),
    QUIZ_REPORT_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 신고입니다."),
    QUIZ_REPORT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 신고에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
