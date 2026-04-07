package com.eof.back.global.exception.exceptions;

import com.eof.back.global.exception.errorCode.ImageErrorCode;

/**
 * 이미지 업로드/삭제에서 발생하는 예외입니다.
 *
 * <p>{@link ImageErrorCode} 의 값과 (optional) 내부 로그 메시지를 담습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException} 의 구현 클래스입니다.
 *
 * <p><b>주요 생성자:</b></p>
 * <ul>
 *   <li>{@code ImageException(ImageErrorCode errorCode)}: 내부 로그 메시지를 담지 않는 예외를 생성합니다.</li>
 *   <li>{@code ImageException(ImageErrorCode errorCode, String logMessage)}: 내부 로그 메시지를 담는 예외를 생성합니다.</li>
 *   <li>{@code ImageException(ImageErrorCode errorCode, String logMessage, String clientMessage)}: 클라이언트로의 메시지 및 내부 로그 메시지를 담는 예외를 생성합니다.</li>
 * </ul>
 *
 * @author Minji-032
 * @see ImageErrorCode
 * @see BaseException
 * @since 2026-04-06
 */
public class ImageException extends BaseException {

    public ImageException(ImageErrorCode errorCode) {
        super(errorCode);
    }

    public ImageException(ImageErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public ImageException(ImageErrorCode errorCode, String logMessage, String clientMessage) {
        super(errorCode, logMessage, clientMessage);
    }
}
