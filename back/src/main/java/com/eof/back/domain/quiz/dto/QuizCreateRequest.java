package com.eof.back.domain.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuizCreateRequest(
        @NotBlank(message = "퀴즈 내용은 필수입니다.")
        @Size(max = 2000, message = "퀴즈 내용은 2000자를 초과할 수 없습니다.")
        String content,

        @NotBlank(message = "정답은 필수입니다.")
        String answer,

        @NotBlank(message = "선택지 1은 필수입니다.")
        String choice1,

        @NotBlank(message = "선택지 2은 필수입니다.")
        String choice2,

        @NotBlank(message = "선택지 3은 필수입니다.")
        String choice3,

        @NotBlank(message = "선택지 4은 필수입니다.")
        String choice4
) {
}
