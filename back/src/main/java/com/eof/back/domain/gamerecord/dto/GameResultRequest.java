package com.eof.back.domain.gamerecord.dto;

import java.util.List;

/**
 * 게임 종료 시 전적 저장을 위한 요청 DTO입니다.
 *
 * @param sessionId 게임 세션 ID
 * @param playerResults 유저별 게임 결과 목록
 *
 * @author Jaewon Ryu
 * @since 2026-03-23
 */
public record GameResultRequest (
        Long sessionId,
        List<PlayerResult> playerResults
) {
    /**
     * 개별 유저의 게임 결과입니다.
     *
     * @param userId 유저 ID
     * @param sessionRanking 해당 게임에서의 순위
     * @param sessionScore 해당 게임에서 획득한 점수
     */
    public record PlayerResult(
            Long userId,
            int sessionRanking,
            int sessionScore
    ) {
    }
}