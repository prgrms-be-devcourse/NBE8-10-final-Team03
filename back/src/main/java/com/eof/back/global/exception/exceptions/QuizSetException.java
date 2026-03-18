package com.eof.back.global.exception.exceptions;

import com.eof.back.global.exception.errorCode.QuizSetErrorCode;

/**
 * 퀴즈 세트 관리에서 발생하는 예외입니다.
 *
 * <p>{@link QuizSetErrorCode} 의 값과 (optional) 내부 로그 메시지를 담습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException} 의 구현 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code QuizSetException(QuizSetErrorCode errorCode)} <br>
 * QuizSetErrorCode만 매개변수로 받도록 강제합니다. 내부 로그 메시지를 담지 않는 예외를 생성합니다. <br>
 * {@code QuizSetException(QuizSetErrorCode errorCode, String logMessage)} <br>
 * QuizSetErrorCode만 매개변수로 받도록 강제합니다. 내부 로그 메시지를 담는 예외를 생성합니다. <br>
 * {@code QuizSetException(QuizSetErrorCode errorCode, String logMessage, String clientMessage)} <br>
 * QuizSetErrorCode만 매개변수로 받도록 강제합니다. 클라이언트로의 메시지 및 내부 로그 메시지를 담는 예외를 생성합니다. <br>
 *
 * @author MintyU
 * @see QuizSetErrorCode
 * @see BaseException
 * @since 2026-03-18
 */
public class QuizSetException extends BaseException {

    public QuizSetException(QuizSetErrorCode errorCode) {
        super(errorCode);
    }

    public QuizSetException(QuizSetErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public QuizSetException(QuizSetErrorCode errorCode, String logMessage, String clientMessage) {
        super(errorCode, logMessage, clientMessage);
    }
}
