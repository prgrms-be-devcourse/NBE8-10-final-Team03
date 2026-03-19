package com.eof.back.domain.quizset.dto;

import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quizset.entity.QuizSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 세트 단건 조회 시 반환되는 데이터 전송 객체입니다.
 * <p>
 * 퀴즈 세트의 기본 정보와 더불어 해당 세트에 포함된 모든 퀴즈 목록을 포함합니다.
 */
@Getter
@Builder
public class QuizSetResponse {

    private Long id;
    private String title;
    private String description;
    private String creatorNickname;
    private Integer totalQuizCount;
    private List<QuizResponse> quizzes;

    /**
     * QuizSet 엔티티로부터 QuizSetResponse DTO를 생성합니다.
     *
     * @param quizSet 퀴즈 세트 엔티티
     * @return 퀴즈 세트 응답 DTO
     */
    public static QuizSetResponse from(QuizSet quizSet) {
        return QuizSetResponse.builder()
                .id(quizSet.getId())
                .title(quizSet.getTitle())
                .description(quizSet.getDescription())
                .creatorNickname(quizSet.getCreator().getNickname())
                .totalQuizCount(quizSet.getTotalQuizCount())
                .quizzes(quizSet.getQuizzes().stream()
                        .map(QuizResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 퀴즈 세트 내 개별 퀴즈 정보를 담는 내부 DTO입니다.
     */
    @Getter
    @Builder
    public static class QuizResponse {
        private Long id;
        private String content;
        private String answer;
        private String choice1;
        private String choice2;
        private String choice3;
        private String choice4;
        private Integer sequence;

        public static QuizResponse from(Quiz quiz) {
            return QuizResponse.builder()
                    .id(quiz.getId())
                    .content(quiz.getContent())
                    .answer(quiz.getAnswer())
                    .choice1(quiz.getChoice1())
                    .choice2(quiz.getChoice2())
                    .choice3(quiz.getChoice3())
                    .choice4(quiz.getChoice4())
                    .sequence(quiz.getSequence())
                    .build();
        }
    }
}
