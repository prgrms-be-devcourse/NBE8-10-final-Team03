package com.eof.back.global.exception.errorCode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 퀴즈 관리에서 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다. 해당 {@code QuizErrorCode} 는 {@link
 * com.eof.back.global.exception.exceptions.QuizException QuizException}에서 사용되며, <br>
 * {@code NAME(HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author MintyU
 * @see ErrorCode
 * @see com.eof.back.global.exception.exceptions.QuizException QuizException
 * @since 2026-03-18
 */
@AllArgsConstructor
public enum QuizErrorCode implements ErrorCode {
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 퀴즈를 찾을 수 없습니다."),
    INVALID_QUIZ_SEQUENCE(HttpStatus.BAD_REQUEST, "유효하지 않은 퀴즈 출제 순서입니다."),
    QUIZ_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "퀴즈 세트의 최대 퀴즈 수를 초과할 수 없습니다.");

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
