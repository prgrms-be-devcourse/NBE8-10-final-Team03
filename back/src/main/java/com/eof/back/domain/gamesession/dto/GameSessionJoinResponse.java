package com.eof.back.domain.gamesession.dto;

import com.eof.back.domain.gamesession.entity.GameSession;

import java.util.List;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author 유재원
 * @see
 * @since 2026-03-23
 */
public record GameSessionJoinResponse(
        Long roomId,
        String title,
        Long quizSetId,
        String status,
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