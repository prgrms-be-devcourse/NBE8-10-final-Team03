package com.eof.back.domain.quiz.dto;

/**
 * 프론트엔드로 문제를 브로드캐스트할 때 사용하는 안전한 DTO입니다.
 * 유저가 개발자 도구로 정답(answer)을 훔쳐보지 못하도록 정답 필드를 제외했습니다.
 * QuizResponse를 받아서 변환합니다.
 *
 * @author 유재원
 * @see QuizResponse
 * @since 2026-03-25
 */
public record QuizBroadcastResponse(
        Long questionId,
        String content,
        String choice1,
        String choice2,
        String choice3,
        String choice4,
        int timeLimit
) {
    // 기존 QuizResponse를 받아서 정답을 빼고 방송용 DTO로 변환하는 메서드
    public static QuizBroadcastResponse from(QuizResponse quiz, int timeLimit) {
        return new QuizBroadcastResponse(
                quiz.getId(),
                quiz.getContent(),
                quiz.getChoice1(),
                quiz.getChoice2(),
                quiz.getChoice3(),
                quiz.getChoice4(),
                timeLimit
        );
    }
}