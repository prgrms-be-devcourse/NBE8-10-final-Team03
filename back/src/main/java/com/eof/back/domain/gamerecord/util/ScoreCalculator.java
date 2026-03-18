package com.eof.back.domain.gamerecord.util;

/**
 * 게임 종료 시 랭킹 포인트를 계산하는 유틸리티 클래스입니다.
 * <p>
 * 순위 기본점수에 참가 인원과 문제 수에 따른 보너스를 적용하여
 * 최종 랭킹 포인트를 산출합니다.
 * <p>
 * 공식: 순위 기본점수 × (1 + 인원수 × 0.1) × (1 + 문제수 × 0.05)
 *
 * @author Jaewon Ryu
 * @see com.eof.back.domain.gamerecord.entity.GameRecord
 * @since 2026-03-18
 */

public class ScoreCalculator {

    private static final int PARTICIPANT_WEIGHT_PERCENT = 10;;
    private static final int QUESTION_WEIGHT_PERCENT = 5;

    private ScoreCalculator() {
    }

    /**
     * 랭킹 포인트를 계산합니다.
     *
     * @param sessionRanking   최종 순위 (1위부터)
     * @param participantCount 참가 인원수
     * @param maxQuestions      출제 문제수
     * @return 가중치가 적용된 랭킹 포인트
     */
    public static long calculateFinalScore(int sessionRanking, int participantCount, int maxQuestions) {
        long baseScore = getBaseScore(sessionRanking);
        long participantBonus = 100 + ((long) participantCount * PARTICIPANT_WEIGHT_PERCENT);
        long questionBonus = 100 + ((long) maxQuestions * QUESTION_WEIGHT_PERCENT);
        return Math.round((double) (baseScore * participantBonus * questionBonus) / 10000);
    }

    /**
     * 순위에 따른 기본점수를 반환합니다.
     *
     * @param ranking 순위
     * @return 기본점수 (1등=100, 2등=60, 3등=30, 4등 이하=10)
     */
    private static long getBaseScore(int ranking) {
        return switch (ranking) {
            case 1 -> 100L;
            case 2 -> 60L;
            case 3 -> 30L;
            default -> 10L;
        };
    }
}