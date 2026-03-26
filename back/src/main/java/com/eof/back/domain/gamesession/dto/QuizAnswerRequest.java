package com.eof.back.domain.gamesession.dto;

/**
 * 퀴즈 정답을 클라이언트에서 서버로 보낼때 사용하는 DTO입니다.
 *
 * @author 유재원
 * @see
 * @since 2026-03-26
 */
public record QuizAnswerRequest(
        String answer
) {
}