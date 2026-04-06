package com.eof.back.domain.quiz.dto;

import com.eof.back.domain.quiz.entity.AnswerType;
import com.eof.back.domain.quiz.entity.QuestionType;
import com.eof.back.domain.quiz.entity.Quiz;
import lombok.Builder;

/**
 * 퀴즈(Quiz) 정보를 반환하기 위한 데이터 전송 객체입니다.
 *
 * @author MintyU
 * @since 2026-03-24
 */
public record QuizResponse(
        Long id,
        QuestionType questionType,
        AnswerType answerType,
        String content,
        String answer,
        String imageUrl,
        String videoUrl,
        Integer startTime,
        Integer endTime,
        String choice1,
        String choice2,
        String choice3,
        String choice4
) {
    /**
     * 빌더 패턴을 위한 콤팩트 생성자입니다.
     */
    @Builder
    public QuizResponse {}

    /**
     * Quiz 엔티티로부터 QuizResponse DTO를 생성합니다.
     *
     * @param quiz 퀴즈 엔티티
     * @return 퀴즈 응답 DTO
     */
    public static QuizResponse from(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .questionType(quiz.getQuestionType())
                .answerType(quiz.getAnswerType())
                .content(quiz.getContent())
                .answer(quiz.getAnswer())
                .imageUrl(quiz.getImageUrl())
                .videoUrl(quiz.getVideoUrl())
                .startTime(quiz.getStartTime())
                .endTime(quiz.getEndTime())
                .choice1(quiz.getChoice1())
                .choice2(quiz.getChoice2())
                .choice3(quiz.getChoice3())
                .choice4(quiz.getChoice4())
                .build();
    }
}
