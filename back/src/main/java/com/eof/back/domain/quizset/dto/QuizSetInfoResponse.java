package com.eof.back.domain.quizset.dto;

import com.eof.back.domain.quizset.entity.QuizSet;
import lombok.Builder;

/**
 * 퀴즈 세트의 기본 정보만을 포함하는 데이터 전송 객체입니다.
 * <p>
 * 퀴즈 목록(quizzes)을 제외한 메타데이터만을 제공하며, 권한에 관계없이 조회가 필요한 경우 사용됩니다.
 *
 * @author MintyU
 * @since 2026-04-02
 */
public record QuizSetInfoResponse(
        Long id,
        String title,
        String description,
        String creatorNickname,
        Integer totalQuizCount
) {
    /**
     * 빌더 패턴을 위한 콤팩트 생성자입니다.
     */
    @Builder
    public QuizSetInfoResponse {}

    /**
     * QuizSet 엔티티로부터 QuizSetInfoResponse DTO를 생성합니다.
     *
     * @param quizSet 퀴즈 세트 엔티티
     * @return 퀴즈 세트 정보 응답 DTO
     */
    public static QuizSetInfoResponse from(QuizSet quizSet) {
        return QuizSetInfoResponse.builder()
                .id(quizSet.getId())
                .title(quizSet.getTitle())
                .description(quizSet.getDescription())
                .creatorNickname(quizSet.getCreator().getNickname())
                .totalQuizCount(quizSet.getTotalQuizCount())
                .build();
    }
}
