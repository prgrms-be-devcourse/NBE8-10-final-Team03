package com.eof.back.domain.user.quizsetbookmark.service;

import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkCreateResponse;
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

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author 5h6vm
 * @see
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

        return new BookmarkCreateResponse(savedBookmark.getId(), quizSetId);
    }
}