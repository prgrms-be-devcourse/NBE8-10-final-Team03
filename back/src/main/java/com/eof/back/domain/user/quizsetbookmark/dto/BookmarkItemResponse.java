package com.eof.back.domain.user.quizsetbookmark.dto;

import com.eof.back.domain.user.quizsetbookmark.entity.QuizSetBookmark;

/**
 * 북마크 목록 조회 시 각 항목을 나타내는 응답 DTO입니다.
 * <p>
 * 북마크 대상 퀴즈셋 ID를 담습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@link #from(QuizSetBookmark)} 팩토리 메서드를 통해 엔티티로부터 생성합니다.
 *
 * @author 5h6vm
 * @see QuizSetBookmark
 * @since 2026-03-25
 */
public record BookmarkItemResponse(
        Long quizSetId,
        String title,
        String description,
        Integer totalQuizCount
) {
    public static BookmarkItemResponse from(QuizSetBookmark bookmark) {
        var quizSet = bookmark.getQuizSet();
        return new BookmarkItemResponse(
                quizSet.getId(),
                quizSet.getTitle(),
                quizSet.getDescription(),
                quizSet.getTotalQuizCount()
        );
    }
}
