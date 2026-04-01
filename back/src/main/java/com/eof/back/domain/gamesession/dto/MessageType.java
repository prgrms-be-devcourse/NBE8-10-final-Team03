package com.eof.back.domain.gamesession.dto;

/**
 * 웹소켓 통신 시 발행되는 메시지의 종류를 식별하기 위한 Enum 클래스입니다.
 * <p>
 * 단일 웹소켓 채널(/chat)을 통해 여러 종류의 이벤트가 전달될 때,
 * 프론트엔드 클라이언트가 메시지의 성격을 파악하고 알맞은 UI 처리(일반 채팅, 시스템 알림, 퀴즈 등)를
 * 분기할 수 있도록 기준값을 제공합니다.
 *
 * @author 유재원
 * @see GameMessageResponse
 * @since 2026-03-25
 */
public enum MessageType {
    ENTER,  // 입장
    LEAVE,  // 퇴장
    CHAT,    // 일반 채팅
    ROOM_ENDED, // 방삭제
    QUIZ, // 퀴즈
    RESULT,
    QUIZ_END,
    ERROR
}
