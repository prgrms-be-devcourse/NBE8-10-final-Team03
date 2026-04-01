package com.eof.back.domain.gamesession.dto;

import com.eof.back.domain.gamesession.entity.GameSession;

import java.util.List;

/**
 * 클라이언트가 게임 세션(방)에 참가하거나 조회할 때 방의 상세 정보와 참가자 목록을 전달하는 응답용 DTO입니다.
 * <p>
 * {@code GameSession} 엔티티를 입력받아 내부 참가자 목록을 순회하며 방장 여부를 판별해
 * 내부 레코드인 {@code PlayerInfo}로 변환합니다. 이후 정적 팩토리 메서드({@code from})를 통해
 * 클라이언트가 필요로 하는 최종 데이터 스펙으로 조립하여 반환합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 자바의 {@code record} 타입이므로 내부적으로 {@code java.lang.Record}를 암묵적으로 상속받습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 필드로 선언된 모든 컴포넌트를 매개변수로 받는 표준 생성자(Canonical Constructor)가 자동 생성됩니다.<br>
 * 외부에서는 주로 정적 팩토리 메서드인 {@code from(GameSession session)}을 통해 객체를 생성합니다.
 *
 * <p><b>빈 관리:</b><br>
 * 스프링 빈(Bean)으로 관리되지 않으며, API 응답 시점에 임시로 생성되어 JSON 직렬화 후 소멸됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 롬복(Lombok) 등의 외부 라이브러리 없이 순수 자바 16 이상의 {@code record} 문법과 Stream API를 활용해 작성되었습니다.
 *
 * @author 유재원
 * @see com.eof.back.domain.gamesession.entity.GameSession
 * @since 2026-03-23
 */
public record GameSessionJoinResponse(
        Long gameSessionId,
        String title,
        Long quizSetId,
        String status,
        int maxPlayers,
        int maxQuizzes,
        List<PlayerInfo> players
) {
    /**
     * GameSession 엔티티를 받아 응답 DTO로 변환하는 정적 팩토리 메서드입니다.
     */
    public static GameSessionJoinResponse from(GameSession session) {

        // 1. 플레이어 목록을 PlayerInfo DTO로 변환
        List<PlayerInfo> playerInfos = session.getPlayers().stream()
                .map(player -> new PlayerInfo(
                        player.getId(),
                        player.getNickname(),
                        player.getId().equals(session.getHost().getId()) // 방장 여부 판별
                ))
                .toList();

        // 2. 최종 DTO 조립 및 반환
        return new GameSessionJoinResponse(
                session.getId(),
                session.getRoomName(),
                session.getQuizSet().getId(),
                session.getStatus().name(),
                session.getMaxPlayers(),
                session.getMaxQuizzes(),
                playerInfos
        );
    }

    // 내부 레코드 (개별 플레이어 정보)
    public record PlayerInfo(
            Long userId,
            String nickname,
            boolean isHost
    ) {
    }
}