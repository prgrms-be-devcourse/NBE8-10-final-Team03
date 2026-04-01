package com.eof.back.domain.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 새로운 퀴즈 생성을 위한 요청 데이터 전송 객체(DTO)입니다.
 * <p>
 * 퀴즈의 내용, 정답, 그리고 4개의 선택지 정보를 포함합니다.
 * 모든 필드는 필수 값이며, {@code jakarta.validation} 어노테이션을 통해 유효성을 검증합니다.
 *
 * @param content 퀴즈 문제 내용 (최대 2000자)
 * @param answer  퀴즈의 정답 내용
 * @param choice1 첫 번째 선택지
 * @param choice2 두 번째 선택지
 * @param choice3 세 번째 선택지
 * @param choice4 네 번째 선택지
 * @author MintyU
 * @since 2026-03-24
 */
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
