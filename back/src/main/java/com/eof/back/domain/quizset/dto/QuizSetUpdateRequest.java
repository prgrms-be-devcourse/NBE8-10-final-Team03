package com.eof.back.domain.quizset.dto;

import jakarta.validation.constraints.Size;

/**
 * 퀴즈 세트 수정을 위한 요청 데이터 전송 객체(DTO)입니다.
 * <p>
 * PATCH 메서드 특성에 맞춰 모든 필드는 선택 사항입니다.
 * 수정을 원하는 필드만 값을 채워서 요청하며, 값이 전달되지 않은 필드(null)는 기존 정보를 유지합니다.
 *
 * @param title       수정할 퀴즈 세트 제목
 * @param description 수정할 퀴즈 세트 설명 (최대 1000자)
 * @author MintyU
 * @since 2026-03-25
 */
public record QuizSetUpdateRequest(
        String title,
        @Size(max = 1000, message = "퀴즈 세트 설명은 1000자를 초과할 수 없습니다.")
        String description
) {
}
