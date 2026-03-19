package com.eof.back.domain.quizset.dto;

import com.eof.back.domain.quizset.entity.QuizSet;
import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 세트 목록 조회 시 반환되는 데이터 전송 객체입니다.
 * <p>
 * 목록의 각 항목으로서 퀴즈 세트의 기본 정보를 제공하며, 상세 퀴즈 목록은 포함하지 않습니다.
 */
@Getter
@Builder
public class QuizSetListResponse {

    private Long id;
    private String title;
    private String description;
    private String creatorNickname;
    private Integer totalQuizCount;

    /**
     * QuizSet 엔티티로부터 QuizSetListResponse DTO를 생성합니다.
     *
     * @param quizSet 퀴즈 세트 엔티티
     * @return 퀴즈 세트 목록 항목 응답 DTO
     */
    public static QuizSetListResponse from(QuizSet quizSet) {
        return QuizSetListResponse.builder()
                .id(quizSet.getId())
                .title(quizSet.getTitle())
                .description(quizSet.getDescription())
                .creatorNickname(quizSet.getCreator().getNickname())
                .totalQuizCount(quizSet.getTotalQuizCount())
                .build();
    }
}
