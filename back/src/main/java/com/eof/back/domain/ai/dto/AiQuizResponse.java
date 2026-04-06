package com.eof.back.domain.ai.dto;

/**
 * Gemini API 응답을 파싱한 개별 퀴즈 데이터 DTO입니다.
 * <p>
 * Gemini API가 반환하는 JSON 배열의 각 항목을 매핑하며,
 * 파싱 후 {@link com.eof.back.domain.quiz.entity.Quiz} 엔티티 생성에 사용됩니다.
 *
 * @author Jaewon Ryu
 * @since 2026-04-02
 */
public record AiQuizResponse(
        String content,
        String answer,
        String choice1,
        String choice2,
        String choice3,
        String choice4
) {}