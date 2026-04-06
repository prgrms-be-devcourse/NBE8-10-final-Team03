package com.eof.back.domain.ai.dto;

/**
 * AI 퀴즈 생성 API의 응답 DTO입니다.
 * <p>
 * Gemini API를 통해 생성된 퀴즈셋의 ID를 반환합니다.
 * 클라이언트는 반환된 {@code quizSetId}를 사용하여 방 생성 시 퀴즈셋을 지정할 수 있습니다.
 *
 * @author Jaewon Ryu
 * @since 2026-04-02
 */
public record AiQuizGenerateResponse(
        Long quizSetId
) {}