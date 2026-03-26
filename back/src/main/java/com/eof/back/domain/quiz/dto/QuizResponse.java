package com.eof.back.domain.quiz.dto;

import com.eof.back.domain.quiz.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈(Quiz) 정보를 반환하기 위한 데이터 전송 객체입니다.
 *
 * @author MintyU
 * @since 2026-03-24
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private Long id;
    private String content;
    private String answer;
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;

    /**
     * Quiz 엔티티로부터 QuizResponse DTO를 생성합니다.
     *
     * @param quiz 퀴즈 엔티티
     * @return 퀴즈 응답 DTO
     */
    public static QuizResponse from(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .content(quiz.getContent())
                .answer(quiz.getAnswer())
                .choice1(quiz.getChoice1())
                .choice2(quiz.getChoice2())
                .choice3(quiz.getChoice3())
                .choice4(quiz.getChoice4())
                .build();
    }
}
