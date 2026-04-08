package com.eof.back.domain.quizset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 퀴즈 세트 생성을 위한 요청 데이터 전송 객체(DTO)입니다.
 * <p>
 * 클라이언트로부터 퀴즈 세트 제작에 필요한 필수 데이터(제목) 및 부가 정보(설명)를 수집합니다.
 * </p>
 *
 * @author MintyU
 * @since 2026-03-19
 */
public record QuizSetCreateRequest(
        @NotBlank(message = "퀴즈 세트 제목은 필수입니다.")
        @Size(max = 30, message = "퀴즈 세트 제목은 30자를 초과할 수 없습니다.")
        String title,

        @Size(max = 255, message = "퀴즈 세트 설명은 255자를 초과할 수 없습니다.")
        String description,

        String thumbnailUrl
) {
    @Builder
    public QuizSetCreateRequest {}
}
