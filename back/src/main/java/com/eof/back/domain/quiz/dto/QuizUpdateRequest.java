package com.eof.back.domain.quiz.dto;

import jakarta.validation.constraints.Size;

/**
 * 퀴즈 수정을 위한 요청 DTO입니다.
 * <p>
 * PATCH 메서드 특성에 맞춰 모든 필드는 선택 사항입니다.
 * 수정을 원하는 필드만 값을 채워서 요청하며, 값이 전달되지 않은 필드(null)는 기존 정보를 유지합니다.
 *
 * @param content 수정할 퀴즈 내용 (최대 2000자)
 * @param answer  수정할 정답
 * @param choice1 수정할 선택지 1
 * @param choice2 수정할 선택지 2
 * @param choice3 수정할 선택지 3
 * @param choice4 수정할 선택지 4
 * @author MintyU
 * @since 2026-03-24
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
