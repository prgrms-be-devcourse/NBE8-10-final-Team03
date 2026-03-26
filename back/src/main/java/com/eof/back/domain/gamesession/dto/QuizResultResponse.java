package com.eof.back.domain.gamesession.dto;

import java.util.List;

/**
 * 퀴즈가 진행될때 라운드가 끝나면 결과를 전송하는 DTO입니다.
 *
 * @author 유재원
 * @see
 * @since 2026-03-26
 */
public record QuizResultResponse(
        String correctAnswer,           // 실제 정답
        List<String> correctUsernames,  // 이번 라운드 정답자 명단
        List<PlayerScore> scoreboard    // 현재 순위표
) {
    public record PlayerScore(
            String username,
            int score
    ) {
    }
}