package com.eof.back.domain.ranking.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 랭킹 조회 API의 응답 DTO입니다.
 * <p>
 * 상위 랭킹 사용자 목록을 담아 반환합니다.
 *
 * @author Jaewon Ryu
 * @see RankingItem
 * @since 2026-03-23
 */
public record RankingResponse(
        Long myRank,
        List<RankingItem> rankings
) implements Serializable {
    /**
     * 랭킹 항목을 나타내는 레코드입니다.
     *
     * @param rank 순위
     * @param nickname 사용자 닉네임
     * @param score 누적 랭킹 점수
     */
    public record RankingItem(
            int rank,
            String nickname,
            long score
    ) implements Serializable {}
}