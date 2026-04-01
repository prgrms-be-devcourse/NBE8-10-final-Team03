package com.eof.back.domain.user.quizsetbookmark.service;

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

    /**
     * 사용자가 특정 퀴즈셋을 북마크합니다.
     *
     * @param userId    북마크를 등록할 사용자의 식별자
     * @param quizSetId 북마크 대상 퀴즈셋의 식별자
     * @throws com.eof.back.global.exception.exceptions.AuthException    사용자가 존재하지 않는 경우
     * @throws com.eof.back.global.exception.exceptions.QuizSetException 퀴즈셋이 존재하지 않거나 이미 북마크한 경우
     */
    void createBookmark(Long userId, Long quizSetId);

    /**
     * 사용자가 등록한 특정 퀴즈셋 북마크를 제거합니다.
     *
     * @param userId    북마크를 제거할 사용자의 식별자
     * @param quizSetId 북마크를 제거할 퀴즈셋의 식별자
     * @throws com.eof.back.global.exception.exceptions.QuizSetException 북마크가 존재하지 않는 경우
     */
    void deleteBookmark(Long userId, Long quizSetId);

    /**
     * 사용자의 북마크 목록을 최신순으로 조회합니다.
     *
     * @param userId 조회할 사용자의 식별자
     * @return 북마크 항목 목록 (북마크 ID, 퀴즈셋 ID)
     * @throws com.eof.back.global.exception.exceptions.AuthException 사용자가 존재하지 않는 경우
     */
    List<BookmarkItemResponse> getBookmarks(Long userId);
}
