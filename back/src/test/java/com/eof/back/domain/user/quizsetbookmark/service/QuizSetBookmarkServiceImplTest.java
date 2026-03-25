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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
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

    @Nested
    @DisplayName("createBookmark")
    class CreateBookmark {

        @Test
        @DisplayName("성공 - 북마크를 생성하고 bookmarkId와 quizSetId를 반환한다")
        void success() {
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
            given(quizSetBookmarkRepository.saveAndFlush(any(QuizSetBookmark.class))).willReturn(savedBookmark);

            // when
            BookmarkCreateResponse response = quizSetBookmarkService.createBookmark(userId, quizSetId);

            // then
            assertThat(response.bookmarkId()).isEqualTo(100L);
            assertThat(response.quizSetId()).isEqualTo(quizSetId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
        void fail_userNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quizSetBookmarkService.createBookmark(999L, 10L))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 퀴즈셋이면 QUIZ_SET_NOT_FOUND 예외가 발생한다")
        void fail_quizSetNotFound() {
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
        @DisplayName("실패 - 이미 북마크한 퀴즈셋이면 QUIZ_SET_BOOKMARK_ALREADY_EXISTS 예외가 발생한다")
        void fail_alreadyExists() {
            // given
            Long userId = 1L;
            Long quizSetId = 10L;

            User user = User.builder().build();
            QuizSet quizSet = QuizSet.builder().title("테스트 퀴즈셋").build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(quizSetRepository.findById(quizSetId)).willReturn(Optional.of(quizSet));
            given(quizSetBookmarkRepository.saveAndFlush(any(QuizSetBookmark.class)))
                    .willThrow(new DataIntegrityViolationException("unique constraint violation"));

            // when & then
            assertThatThrownBy(() -> quizSetBookmarkService.createBookmark(userId, quizSetId))
                    .isInstanceOf(QuizSetException.class)
                    .satisfies(e -> assertThat(((QuizSetException) e).getErrorCode())
                            .isEqualTo(QuizSetErrorCode.QUIZ_SET_BOOKMARK_ALREADY_EXISTS));
        }
    }

    @Nested
    @DisplayName("deleteBookmark")
    class DeleteBookmark {

        @Test
        @DisplayName("성공 - 북마크를 삭제한다")
        void success() {
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
        @DisplayName("실패 - 북마크하지 않은 퀴즈셋이면 QUIZ_SET_BOOKMARK_NOT_FOUND 예외가 발생한다")
        void fail_notFound() {
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

    @Nested
    @DisplayName("getBookmarks")
    class GetBookmarks {

        @Test
        @DisplayName("성공 - 북마크 목록을 반환한다")
        void success() {
            // given
            Long userId = 1L;

            User user = User.builder().build();
            ReflectionTestUtils.setField(user, "id", userId);

            QuizSet quizSet1 = QuizSet.builder().title("퀴즈셋1").build();
            ReflectionTestUtils.setField(quizSet1, "id", 10L);

            QuizSet quizSet2 = QuizSet.builder().title("퀴즈셋2").build();
            ReflectionTestUtils.setField(quizSet2, "id", 20L);

            QuizSetBookmark bookmark1 = QuizSetBookmark.of(user, quizSet1);
            ReflectionTestUtils.setField(bookmark1, "id", 100L);

            QuizSetBookmark bookmark2 = QuizSetBookmark.of(user, quizSet2);
            ReflectionTestUtils.setField(bookmark2, "id", 200L);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(quizSetBookmarkRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                    .willReturn(List.of(bookmark1, bookmark2));

            // when
            List<BookmarkItemResponse> result = quizSetBookmarkService.getBookmarks(userId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).bookmarkId()).isEqualTo(100L);
            assertThat(result.get(0).quizSetId()).isEqualTo(10L);
            assertThat(result.get(1).bookmarkId()).isEqualTo(200L);
            assertThat(result.get(1).quizSetId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("성공 - 북마크가 없으면 빈 리스트를 반환한다")
        void success_empty() {
            // given
            Long userId = 1L;
            User user = User.builder().build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(quizSetBookmarkRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                    .willReturn(List.of());

            // when
            List<BookmarkItemResponse> result = quizSetBookmarkService.getBookmarks(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 USER_NOT_FOUND 예외가 발생한다")
        void fail_userNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quizSetBookmarkService.getBookmarks(999L))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_NOT_FOUND));
        }
    }
}
