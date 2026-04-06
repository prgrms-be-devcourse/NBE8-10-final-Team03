package com.eof.back.global.exception.errorCode;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 이미지 업로드/삭제에서 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 해당 {@code ImageErrorCode} 는
 * {@link com.eof.back.global.exception.exceptions.ImageException ImageException}에서 사용되며, <br>
 * {@code NAME(HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author Minji-032
 * @see ErrorCode
 * @see com.eof.back.global.exception.exceptions.ImageException ImageException
 * @since 2026-04-06
 */
@AllArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "빈 파일입니다."),
    IMAGE_INVALID_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않는 확장자입니다. (jpg, jpeg, png, gif, webp)"),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 초과되었습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.");

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
