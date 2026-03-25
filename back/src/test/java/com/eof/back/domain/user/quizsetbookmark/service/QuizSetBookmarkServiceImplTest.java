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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class QuizSetBookmarkServiceImplTest {

    @Mock
    private QuizSetBookmarkRepository quizSetBookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizSetRepository quizSetRepository;

    @InjectMocks
    private QuizSetBookmarkServiceImpl quizSetBookmarkService;

    @Test
    @DisplayName("북마크 생성 성공")
    void createBookmark_success() {
        // given
        Long userId = 1L;
        Long quizSetId = 10L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        QuizSet quizSet = QuizSet.builder().title("테스트 퀴즈셋").build();
        ReflectionTestUtils.setField(quizSet, "id", quizSetId);

        QuizSetBookmark savedBookmark = QuizSetBookmark.of(user, quizSet);
        ReflectionTestUtils.setField(savedBookmark, "id", 100L);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(quizSetRepository.findById(quizSetId)).willReturn(Optional.of(quizSet));
        given(quizSetBookmarkRepository.existsByUserIdAndQuizSetId(userId, quizSetId)).willReturn(false);
        given(quizSetBookmarkRepository.save(any(QuizSetBookmark.class))).willReturn(savedBookmark);

        // when
        BookmarkCreateResponse response = quizSetBookmarkService.createBookmark(userId, quizSetId);

        // then
        assertThat(response.bookmarkId()).isEqualTo(100L);
        assertThat(response.quizSetId()).isEqualTo(quizSetId);
    }

    @Test
    @DisplayName("북마크 생성 실패 - 존재하지 않는 사용자")
    void createBookmark_userNotFound() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> quizSetBookmarkService.createBookmark(999L, 10L))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("북마크 생성 실패 - 존재하지 않는 퀴즈셋")
    void createBookmark_quizSetNotFound() {
        // given
        Long userId = 1L;
        User user = User.builder().build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(quizSetRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> quizSetBookmarkService.createBookmark(userId, 999L))
                .isInstanceOf(QuizSetException.class)
                .satisfies(e -> assertThat(((QuizSetException) e).getErrorCode())
                        .isEqualTo(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));
    }

    @Test
    @DisplayName("북마크 생성 실패 - 이미 북마크한 퀴즈셋")
    void createBookmark_alreadyExists() {
        // given
        Long userId = 1L;
        Long quizSetId = 10L;

        User user = User.builder().build();
        QuizSet quizSet = QuizSet.builder().title("테스트 퀴즈셋").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(quizSetRepository.findById(quizSetId)).willReturn(Optional.of(quizSet));
        given(quizSetBookmarkRepository.existsByUserIdAndQuizSetId(userId, quizSetId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> quizSetBookmarkService.createBookmark(userId, quizSetId))
                .isInstanceOf(QuizSetException.class)
                .satisfies(e -> assertThat(((QuizSetException) e).getErrorCode())
                        .isEqualTo(QuizSetErrorCode.QUIZ_SET_BOOKMARK_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("북마크 제거 성공")
    void deleteBookmark_success() {
        // given
        Long userId = 1L;
        Long quizSetId = 10L;

        given(quizSetBookmarkRepository.deleteByUserIdAndQuizSetId(userId, quizSetId)).willReturn(1);

        // when
        quizSetBookmarkService.deleteBookmark(userId, quizSetId);

        // then
        then(quizSetBookmarkRepository).should(times(1)).deleteByUserIdAndQuizSetId(userId, quizSetId);
    }

    @Test
    @DisplayName("북마크 제거 실패 - 북마크하지 않은 퀴즈셋")
    void deleteBookmark_notFound() {
        // given
        Long userId = 1L;
        Long quizSetId = 10L;

        given(quizSetBookmarkRepository.deleteByUserIdAndQuizSetId(userId, quizSetId)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> quizSetBookmarkService.deleteBookmark(userId, quizSetId))
                .isInstanceOf(QuizSetException.class)
                .satisfies(e -> assertThat(((QuizSetException) e).getErrorCode())
                        .isEqualTo(QuizSetErrorCode.QUIZ_SET_BOOKMARK_NOT_FOUND));
    }
}
