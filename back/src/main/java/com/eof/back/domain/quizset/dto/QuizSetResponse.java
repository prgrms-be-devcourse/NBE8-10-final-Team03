package com.eof.back.domain.quizset.dto;

import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quizset.entity.QuizSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;

/**
 * 퀴즈 세트 단건 조회 시 반환되는 데이터 전송 객체입니다.
 * <p>
 * 퀴즈 세트의 기본 정보와 더불어 해당 세트에 포함된 모든 퀴즈 목록을 포함합니다.
 *
 * @author MintyU
 * @since 2026-03-24
 */
public record QuizSetResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        String creatorNickname,
        Integer totalQuizCount,
        List<QuizResponse> quizzes
) {
    /**
     * 빌더 패턴을 위한 콤팩트 생성자입니다.
     */
    @Builder
    public QuizSetResponse {}

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
                .thumbnailUrl(quizSet.getThumbnailUrl())
                .creatorNickname(quizSet.getCreator().getNickname())
                .totalQuizCount(quizSet.getTotalQuizCount())
                .quizzes(quizSet.getQuizzes().stream()
                        .map(QuizResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
