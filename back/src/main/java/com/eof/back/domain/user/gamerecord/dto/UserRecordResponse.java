package com.eof.back.domain.user.gamerecord.dto;

import com.eof.back.domain.user.gamerecord.entity.GameRecord;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 유저의 게임 전적 조회 응답 DTO입니다.
 *
 * @author Jaewon Ryu
 * @since 2026-03-19
 */
public record UserRecordResponse(
        long totalGames,
        long totalWins,
        long totalRankingScore,
        List<RecordItem> recentRecords,
        int page,
        int size,
        long totalElements
) {

    /**
     * 개별 게임 기록 항목
     */
    public record RecordItem(
            Long sessionId,
            String quizSetTitle,
            int maxPlayers,
            int sessionRanking,
            int sessionScore,
            long earnedRankingScore,
            LocalDateTime playedAt
    ) {
        /**
         * GameRecord 엔티티를 RecordItem으로 변환합니다.
         *
         * @param record GameRecord 엔티티
         * @return 변환된 RecordItem
         */
        public static RecordItem from(GameRecord record) {
            return new RecordItem(
                    record.getGameSession().getId(),
                    record.getGameSession().getQuizSet().getTitle(),
                    record.getGameSession().getMaxPlayers(),
                    record.getSessionRanking(),
                    record.getSessionScore(),
                    record.getEarnedRankingScore(),
                    record.getCreatedAt()
            );
        }
    }
}