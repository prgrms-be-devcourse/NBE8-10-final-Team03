package com.eof.back.domain.gamesession.dto;

import com.eof.back.domain.gamesession.controller.GameMessageController;

/**
 * 클라이언트(프론트엔드)가 서버로 일반 채팅 메시지를 전송할 때 사용하는 요청(Request) DTO입니다.
 * <p>
 * 사용자가 입력한 순수 텍스트 데이터를 담아 웹소켓(STOMP) 채널을 통해 서버로 전달하며,
 * 서버는 이 값을 가공하여 방에 있는 다른 유저들에게 브로드캐스트(Broadcast)합니다.
 *
 * @author 유재원
 * @see GameMessageController
 * @since 2026-03-24
 */
public record ChatMessageRequest(
        String message // 프론트엔드가 입력한 채팅 내용
) {
}