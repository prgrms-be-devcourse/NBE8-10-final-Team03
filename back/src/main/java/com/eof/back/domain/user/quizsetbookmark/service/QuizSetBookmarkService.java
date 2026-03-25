package com.eof.back.domain.user.quizsetbookmark.service;

import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkCreateResponse;
import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkItemResponse;

import java.util.List;

/**
 * 퀴즈셋 북마크 기능에 대한 서비스 인터페이스입니다.
 * <p>
 * 북마크 생성, 제거, 조회 메서드를 정의합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link QuizSetBookmarkServiceImpl}에서 구현됩니다.
 *
 * @author 5h6vm
 * @see QuizSetBookmarkServiceImpl
 * @since 2026-03-24
 */
public interface QuizSetBookmarkService {

    BookmarkCreateResponse createBookmark(Long userId, Long quizSetId);

    void deleteBookmark(Long userId, Long quizSetId);

    List<BookmarkItemResponse> getBookmarks(Long userId);
}
