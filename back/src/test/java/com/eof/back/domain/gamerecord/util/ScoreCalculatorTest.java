package com.eof.back.domain.gamerecord.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculatorTest {

    @Test
    @DisplayName("4명 10문제 1등이면 210점")
    void 일반_게임_1등() {
        long result = ScoreCalculator.calculateFinalScore(1, 4, 10);
        // 100 × 1.4 × 1.5 = 210
        assertThat(result).isEqualTo(210);
    }

    @Test
    @DisplayName("4명 10문제 2등이면 126점")
    void 일반_게임_2등() {
        long result = ScoreCalculator.calculateFinalScore(2, 4, 10);
        // 60 × 1.4 × 1.5 = 126
        assertThat(result).isEqualTo(126);
    }

    @Test
    @DisplayName("8명 20문제 1등이면 360점")
    void 대규모_게임_1등() {
        long result = ScoreCalculator.calculateFinalScore(1, 8, 20);
        // 100 × 1.8 × 2.0 = 360
        assertThat(result).isEqualTo(360);
    }

    @Test
    @DisplayName("2명 5문제 꼴찌면 15점")
    void 소규모_게임_꼴찌() {
        long result = ScoreCalculator.calculateFinalScore(2, 2, 5);
        // 60 × 1.2 × 1.25 = 90
        assertThat(result).isEqualTo(90);
    }

    @Test
    @DisplayName("4등 이하는 전부 기본점수 10")
    void 사등_이하() {
        long result4 = ScoreCalculator.calculateFinalScore(4, 4, 10);
        long result8 = ScoreCalculator.calculateFinalScore(8, 4, 10);
        // 둘 다 10 × 1.4 × 1.5 = 21
        assertThat(result4).isEqualTo(21);
        assertThat(result8).isEqualTo(21);
    }
}