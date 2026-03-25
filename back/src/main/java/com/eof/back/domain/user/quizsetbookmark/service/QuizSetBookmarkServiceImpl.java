package com.eof.back.domain.user.quizsetbookmark.service;

import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkCreateResponse;
import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkItemResponse;
import com.eof.back.domain.user.quizsetbookmark.entity.QuizSetBookmark;
import com.eof.back.domain.user.quizsetbookmark.repository.QuizSetBookmarkRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 퀴즈셋 북마크 기능의 비즈니스 로직을 처리하는 서비스 구현체입니다.
 * <p>
 * 북마크 생성, 제거, 조회를 담당하며, 각 요청에 대해 사용자와 퀴즈셋의 존재 여부 및
 * 중복 북마크 여부를 검증합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link QuizSetBookmarkService}의 구현체입니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service}로 등록되며, 기본적으로 읽기 전용 트랜잭션으로 동작합니다.
 * 쓰기 작업 메서드에는 별도로 {@code @Transactional}이 적용됩니다.
 *
 * @author 5h6vm
 * @see QuizSetBookmarkService
 * @since 2026-03-24
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizSetBookmarkServiceImpl implements QuizSetBookmarkService {

    private final QuizSetBookmarkRepository quizSetBookmarkRepository;
    private final UserRepository userRepository;
    private final QuizSetRepository quizSetRepository;

    @Override
    @Transactional
    public BookmarkCreateResponse createBookmark(Long userId, Long quizSetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));

        if (quizSetBookmarkRepository.existsByUserIdAndQuizSetId(userId, quizSetId)) {
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_BOOKMARK_ALREADY_EXISTS);
        }

        QuizSetBookmark savedBookmark = quizSetBookmarkRepository.save(QuizSetBookmark.of(user, quizSet));

        return BookmarkCreateResponse.from(savedBookmark);
    }

    @Override
    @Transactional
    public void deleteBookmark(Long userId, Long quizSetId) {
        int deleted = quizSetBookmarkRepository.deleteByUserIdAndQuizSetId(userId, quizSetId);

        if (deleted == 0) {
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_BOOKMARK_NOT_FOUND);
        }
    }

    @Override
    public List<BookmarkItemResponse> getBookmarks(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        return quizSetBookmarkRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(BookmarkItemResponse::from)
                .toList();
    }
}
