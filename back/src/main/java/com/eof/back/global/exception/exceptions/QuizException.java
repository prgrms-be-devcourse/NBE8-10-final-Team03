package com.eof.back.global.exception.exceptions;

import com.eof.back.global.exception.errorCode.QuizErrorCode;

/**
 * 퀴즈 관리에서 발생하는 예외입니다.
 *
 * <p>{@link QuizErrorCode} 의 값과 (optional) 내부 로그 메시지를 담습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException} 의 구현 클래스입니다.
 *
 * <p><b>주요 생성자:</b></p>
 * <ul>
 *   <li>{@code QuizException(QuizErrorCode errorCode)}: 내부 로그 메시지를 담지 않는 예외를 생성합니다.</li>
 *   <li>{@code QuizException(QuizErrorCode errorCode, String logMessage)}: 내부 로그 메시지를 담는 예외를 생성합니다.</li>
 *   <li>{@code QuizException(QuizErrorCode errorCode, String logMessage, String clientMessage)}: 클라이언트로의 메시지 및 내부 로그 메시지를 담는 예외를 생성합니다.</li>
 * </ul>
 *
 *
 * @author MintyU
 * @see QuizErrorCode
 * @see BaseException
 * @since 2026-03-18
 */
public class QuizException extends BaseException {

    public QuizException(QuizErrorCode errorCode) {
        super(errorCode);
    }

    public QuizException(QuizErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public QuizException(QuizErrorCode errorCode, String logMessage, String clientMessage) {
        super(errorCode, logMessage, clientMessage);
    }
}
