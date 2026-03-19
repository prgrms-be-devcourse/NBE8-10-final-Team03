package com.eof.back.global.exception.exceptions;

import com.eof.back.global.exception.errorCode.ErrorCode;
import com.eof.back.global.exception.errorCode.GameSessionErrorCode;
import com.eof.back.global.exception.errorCode.QuizErrorCode;

/**
 * 게임 세션(방) 관리 및 비즈니스 로직 처리 중 발생하는 예외입니다.
 *
 * <p>{@link GameSessionErrorCode} 의 값과 (optional) 내부 로그 메시지를 담습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException} 의 구현 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code GameSessionException(GameSessionErrorCode errorCode)} <br>
 * GameSessionErrorCode만 매개변수로 받도록 강제합니다. 내부 로그 메시지를 담지 않는 예외를 생성합니다. <br>
 * {@code GameSessionException(GameSessionErrorCode errorCode, String logMessage)} <br>
 * GameSessionErrorCode와 내부 로그 메시지를 매개변수로 받습니다. 내부 로그 메시지를 담는 예외를 생성합니다. <br>
 * {@code GameSessionException(GameSessionErrorCode errorCode, String logMessage, String clientMessage)} <br>
 * GameSessionErrorCode와 더불어, 클라이언트로 전달할 메시지 및 내부 로그 메시지를 모두 담는 예외를 생성합니다. <br>
 *
 * @author 유재원
 * @see GameSessionErrorCode
 * @see BaseException
 * @since 2026-03-19
 */
public class GameSessionException extends BaseException {

    public GameSessionException(QuizErrorCode errorCode) {
        super(errorCode);
    }

    public GameSessionException(QuizErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public GameSessionException(QuizErrorCode errorCode, String logMessage, String clientMessage) {
        super(errorCode, logMessage, clientMessage);
    }
}
