package com.eof.back.domain.quizset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 세트 생성을 위한 요청 데이터 전송 객체(DTO)입니다.
 * <p>
 * 클라이언트로부터 퀴즈 세트 제작에 필요한 필수 데이터(제목, 총 문제 수) 및 부가 정보(설명)를 수집합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@link lombok.Builder} 를 활용하여 인스턴스를 생성하며, {@link lombok.NoArgsConstructor}와 {@link lombok.AllArgsConstructor}를 포함합니다. <br>
 *
 * @author MintyU
 * @since 2026-03-19
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuizSetCreateRequest {

    @NotBlank(message = "퀴즈 세트 제목은 필수입니다.")
    private String title;

    @Size(max = 1000, message = "퀴즈 세트 설명은 1000자를 초과할 수 없습니다.")
    private String description;

    @NotNull(message = "총 문제 수는 필수입니다.")
    @PositiveOrZero(message = "총 문제 수는 0 이상이어야 합니다.")
    private Integer totalQuizCount;
}
