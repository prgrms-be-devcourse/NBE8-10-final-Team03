package com.eof.back.global.exception.exceptions;

import com.eof.back.global.exception.errorCode.QuizReportErrorCode;
import lombok.Getter;

/**
 * 퀴즈 신고 관련 비즈니스 로직 중 발생하는 예외를 정의합니다.
 * <p>
 * 퀴즈 신고 도메인에서 발생하는 런타임 예외를 캡슐화합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException}을 상속받습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code QuizReportException(QuizReportErrorCode errorCode)} <br>
 * 정의된 에러 코드를 사용하여 예외를 생성합니다. <br>
 *
 * @author MintyU
 * @since 2026-03-27
 */
@Getter
public class QuizReportException extends BaseException {

    private final QuizReportErrorCode errorCode;

    public QuizReportException(QuizReportErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
