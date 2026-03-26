package com.eof.back.domain.gamesession.dto;

/**
 * 서버에서 클라이언트(프론트엔드)로 전송되는 모든 웹소켓 메시지의 통합 응답(Response) DTO입니다.
 * <p>
 * 입장, 퇴장, 일반 채팅, 방 폭파 등 다양한 웹소켓 이벤트를 단일 채널(/chat)에서 처리하기 위해
 * 통일된 규격을 제공합니다. 제네릭 타입 {@code <T>}를 사용하여 상황에 따라 필요한 추가 데이터
 * (예: 갱신된 방 인원 정보)를 유연하게 담을 수 있으며, 정적 팩토리 메서드를 통해
 * 각 이벤트 상황에 맞는 응답 객체를 직관적으로 생성할 수 있습니다.
 *
 * @param <T> 이벤트와 함께 전달할 추가 데이터의 타입 (데이터가 필요 없는 경우 Void)
 * @author 유재원
 * @see MessageType
 * @since 2026-03-25
 */
public record GameMessageResponse<T>(
        MessageType type,
        String sender,
        String message,
        T data
) {
    // 일반 채팅용 팩토리 메서드 (data가 필요 없음)
    public static GameMessageResponse<Void> chat(String sender, String message) {
        return new GameMessageResponse<>(MessageType.CHAT, sender, message, null);
    }

    // 입장용 팩토리 메서드 (방 정보 데이터를 함께 보냄)
    public static <T> GameMessageResponse<T> enter(String sender, String message, T data) {
        return new GameMessageResponse<>(MessageType.ENTER, sender, message, data);
    }

    // 퇴장용 팩토리 메서드
    public static <T> GameMessageResponse<T> leave(String sender, String message, T data) {
        return new GameMessageResponse<>(MessageType.LEAVE, sender, message, data);
    }

    // 방 삭제용 팩토리 메서드
    public static GameMessageResponse<Void> roomDeleted(String message) {
        return new GameMessageResponse<>(MessageType.ROOM_DELETED, "SYSTEM", message, null);
    }

    //문제 정보 전달 메소드
    public static <T> GameMessageResponse<T> quiz(T data) {
        return new GameMessageResponse<>(MessageType.QUIZ, "SYSTEM", "새로운 문제가 출제되었습니다!", data);
    }

    //라운드 종료 후 현재 순위 전달 메소드
    public static <T> GameMessageResponse<T> result(T data) {
        return new GameMessageResponse<>(MessageType.RESULT, "SYSTEM", "라운드 종료! 정답 및 현재 순위입니다.", data);
    }

    //모든 라운드 종료 전달 메소드
    public static GameMessageResponse<Void> quizEnd() {
        return new GameMessageResponse<>(
                MessageType.QUIZ_END, "SYSTEM", "모든 라운드가 종료되었습니다. 수고하셨습니다!", null);
    }

    //에러 전달 메소드
    public static <T> GameMessageResponse<T> error(String message) {
        return new GameMessageResponse<>(
                MessageType.ERROR, "SYSTEM", message, null);
    }
}