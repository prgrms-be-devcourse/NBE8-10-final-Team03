package com.eof.back.domain.quiz.dto;

public record QuizCreateRequest(
        String content,
        String answer,
        String choice1,
        String choice2,
        String choice3,
        String choice4
) {
}
