package com.eof.back.domain.quiz.dto;

import jakarta.validation.constraints.Size;

/**
 * 퀴즈 수정을 위한 요청 DTO입니다.
 * PATCH 메서드 특성에 맞춰 모든 필드는 선택 사항입니다.
 */
public record QuizUpdateRequest(
        @Size(max = 2000, message = "퀴즈 내용은 2000자를 초과할 수 없습니다.")
        String content,
        String answer,
        String choice1,
        String choice2,
        String choice3,
        String choice4
) {
}
