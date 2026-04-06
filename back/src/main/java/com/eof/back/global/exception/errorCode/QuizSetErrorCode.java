package com.eof.back.global.exception.errorCode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 퀴즈 세트 관리에서 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다. 해당 {@code QuizSetErrorCode} 는
 * {@link com.eof.back.global.exception.exceptions.QuizSetException QuizSetException}에서 사용되며, <br>
 * {@code NAME(HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author MintyU
 * @see ErrorCode
 * @see com.eof.back.global.exception.exceptions.QuizSetException QuizSetException
 * @since 2026-03-18
 */
@AllArgsConstructor
public enum QuizSetErrorCode implements ErrorCode {
    QUIZ_SET_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 퀴즈 세트를 찾을 수 없습니다."),
    QUIZ_SET_CREATE_FAIL(HttpStatus.BAD_REQUEST, "퀴즈 세트 생성에 실패하였습니다."),
    QUIZ_SET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 퀴즈 세트에 대한 권한이 없습니다."),

    //북마크
    QUIZ_SET_BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 북마크한 퀴즈 세트입니다."),
    QUIZ_SET_BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "북마크하지 않은 퀴즈 세트입니다."),

    INVALID_TOPIC(HttpStatus.BAD_REQUEST, "부적절하거나 잘못된 주제입니다.");

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
