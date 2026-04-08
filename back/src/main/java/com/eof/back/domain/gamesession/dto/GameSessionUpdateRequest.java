package com.eof.back.domain.gamesession.dto;

import jakarta.validation.constraints.Min;

/**
 * 게임 세션(방) 설정 수정을 위한 요청 DTO입니다.
 */
public record GameSessionUpdateRequest(
        String roomName,
        Long quizSetId,

        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        Integer maxPlayers,

        @Min(value = 1, message = "최소 1문제 이상이어야 합니다.")
        Integer maxQuizzes
) {
}
