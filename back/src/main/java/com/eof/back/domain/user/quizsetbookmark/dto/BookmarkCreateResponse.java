package com.eof.back.domain.user.quizsetbookmark.dto;

import com.eof.back.domain.user.quizsetbookmark.entity.QuizSetBookmark;

/**
 * 북마크 생성 성공 시 반환되는 응답 DTO입니다.
 * <p>
 * 생성된 북마크의 ID와 대상 퀴즈셋 ID를 담습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@link #from(QuizSetBookmark)} 팩토리 메서드를 통해 엔티티로부터 생성합니다.
 *
 * @author 5h6vm
 * @see QuizSetBookmark
 * @since 2026-03-24
 */
public record BookmarkCreateResponse(
        Long bookmarkId,
        Long quizSetId
) {
    public static BookmarkCreateResponse from(QuizSetBookmark bookmark) {
        return new BookmarkCreateResponse(bookmark.getId(), bookmark.getQuizSet().getId());
    }
}
