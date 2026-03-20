package com.eof.back.global.exception.errorCode;

import com.eof.back.global.exception.exceptions.GameSessionException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 게임 세션(방) 관리 및 비즈니스 로직 처리 중 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 HTTP 응답 상태 코드 및 메시지를 가지며, 그 명명 규칙은 팀의 예외 처리 문서를 참조해야 합니다. 해당 {@code GameSessionErrorCode}는 {@link
 * GameSessionException}에서 사용되며, <br>
 * {@code NAME(HttpStatus.STATUS, "some message")} 형태로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author 유재원
 * @see ErrorCode
 * @see GameSessionException
 * @since 2026-03-19
 */

@AllArgsConstructor
public enum GameSessionErrorCode implements ErrorCode {
    // 방 입장 시 유효성 검증
    GAME_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게임세션을 찾을 수 없습니다."),
    PLAYERS_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "플레이어 최대 인원 수를 초과할 수 없습니다."),

    // 방 생성 시 유효성 검증
    INVALID_MAX_PLAYERS(HttpStatus.BAD_REQUEST, "최대 플레이어 수는 최소 2명 이상이어야 합니다."),
    MAX_QUIZZES_EXCEEDED(HttpStatus.BAD_REQUEST, "설정한 퀴즈 수가 퀴즈 세트의 총 문항 수를 초과할 수 없습니다."),

    // 세션 진행 상태 검증
    GAME_ALREADY_STARTED(HttpStatus.CONFLICT, "이미 시작된 게임 세션입니다."),
    GAME_ALREADY_ENDED(HttpStatus.CONFLICT, "이미 종료된 게임 세션입니다."),
    INVALID_GAME_STATUS(HttpStatus.BAD_REQUEST, "현재 게임 상태에서는 처리할 수 없는 요청입니다."),

    // 권한 검증
    UNAUTHORIZED_HOST_ACTION(HttpStatus.FORBIDDEN, "해당 작업은 방장만 수행할 수 있습니다.");

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
