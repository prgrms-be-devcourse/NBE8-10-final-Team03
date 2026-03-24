package com.eof.back.domain.quiz.service;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.entity.User;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizSetRepository quizSetRepository;

    @InjectMocks
    private QuizServiceImpl quizService;

    private User creator;
    private QuizSet quizSet;
    private Long userId = 1L;
    private Long quizSetId = 10L;

    @BeforeEach
    void setUp() {
        creator = User.builder().username("creator").nickname("creator").build();
        ReflectionTestUtils.setField(creator, "id", userId);

        quizSet = QuizSet.builder()
                .title("테스트 퀴즈 세트")
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", quizSetId);
    }

    @Nested
    @DisplayName("퀴즈 생성")
    class CreateQuiz {
        @Test
        @DisplayName("성공 - 퀴즈를 생성하고 세트의 퀴즈 수를 증가시킨다")
        void success() {
            // given
            QuizCreateRequest request = new QuizCreateRequest("문제", "정답", "1", "2", "3", "4");
            Quiz quiz = Quiz.builder()
                    .quizSet(quizSet)
                    .content(request.content())
                    .answer(request.answer())
                    .choice1(request.choice1())
                    .choice2(request.choice2())
                    .choice3(request.choice3())
                    .choice4(request.choice4())
                    .build();
            ReflectionTestUtils.setField(quiz, "id", 100L);

            given(quizSetRepository.findById(quizSetId)).willReturn(Optional.of(quizSet));
            given(quizRepository.save(any(Quiz.class))).willReturn(quiz);

            // when
            Long quizId = quizService.createQuiz(quizSetId, request, userId);

            // then
            assertThat(quizId).isEqualTo(100L);
            assertThat(quizSet.getTotalQuizCount()).isEqualTo(1);
            verify(quizRepository, times(1)).save(any(Quiz.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 퀴즈 세트")
        void fail_quizSetNotFound() {
            // given
            QuizCreateRequest request = new QuizCreateRequest("문제", "정답", "1", "2", "3", "4");
            given(quizSetRepository.findById(quizSetId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quizService.createQuiz(quizSetId, request, userId))
                    .isInstanceOf(QuizSetException.class)
                    .hasFieldOrPropertyWithValue("errorCode", QuizSetErrorCode.QUIZ_SET_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 권한 없음 (제작자가 아님)")
        void fail_noOwnership() {
            // given
            QuizCreateRequest request = new QuizCreateRequest("문제", "정답", "1", "2", "3", "4");
            given(quizSetRepository.findById(quizSetId)).willReturn(Optional.of(quizSet));

            // when & then
            assertThatThrownBy(() -> quizService.createQuiz(quizSetId, request, 999L))
                    .isInstanceOf(AuthException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_AUTH_FAIL);
        }
    }

    @Nested
    @DisplayName("퀴즈 조회")
    class GetQuiz {
        @Test
        @DisplayName("성공 - 단건 조회")
        void success_getQuiz() {
            // given
            Long quizId = 100L;
            Quiz quiz = Quiz.builder().quizSet(quizSet).content("문제").build();
            ReflectionTestUtils.setField(quiz, "id", quizId);

            given(quizRepository.findById(quizId)).willReturn(Optional.of(quiz));

            // when
            QuizResponse response = quizService.getQuiz(quizSetId, quizId);

            // then
            assertThat(response.getId()).isEqualTo(quizId);
            assertThat(response.getContent()).isEqualTo("문제");
        }

        @Test
        @DisplayName("실패 - 퀴즈가 해당 세트에 속하지 않음")
        void fail_notBelongToSet() {
            // given
            Long quizId = 100L;
            Long otherSetId = 999L;
            Quiz quiz = Quiz.builder().quizSet(quizSet).build();
            ReflectionTestUtils.setField(quiz, "id", quizId);

            given(quizRepository.findById(quizId)).willReturn(Optional.of(quiz));

            // when & then
            assertThatThrownBy(() -> quizService.getQuiz(otherSetId, quizId))
                    .isInstanceOf(QuizException.class)
                    .hasFieldOrPropertyWithValue("errorCode", QuizErrorCode.QUIZ_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("퀴즈 목록 조회")
    class GetQuizzesByQuizSetId {
        @Test
        @DisplayName("성공 - 해당 세트의 모든 퀴즈를 조회한다")
        void success() {
            // given
            Quiz quiz1 = Quiz.builder().quizSet(quizSet).content("문제1").build();
            Quiz quiz2 = Quiz.builder().quizSet(quizSet).content("문제2").build();
            ReflectionTestUtils.setField(quizSet, "quizzes", List.of(quiz1, quiz2));

            given(quizSetRepository.findById(quizSetId)).willReturn(Optional.of(quizSet));

            // when
            List<QuizResponse> responses = quizService.getQuizzesByQuizSetId(quizSetId);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getContent()).isEqualTo("문제1");
            assertThat(responses.get(1).getContent()).isEqualTo("문제2");
        }
    }

    @Nested
    @DisplayName("퀴즈 수정")
    class UpdateQuiz {
        @Test
        @DisplayName("성공 - 퀴즈 내용을 수정한다")
        void success() {
            // given
            Long quizId = 100L;
            Quiz quiz = Quiz.builder().quizSet(quizSet).content("이전 내용").build();
            ReflectionTestUtils.setField(quiz, "id", quizId);
            QuizUpdateRequest request = new QuizUpdateRequest("수정된 내용", null, null, null, null, null);

            given(quizRepository.findById(quizId)).willReturn(Optional.of(quiz));

            // when
            Long updatedId = quizService.updateQuiz(quizSetId, quizId, request, userId);

            // then
            assertThat(updatedId).isEqualTo(quizId);
            assertThat(quiz.getContent()).isEqualTo("수정된 내용");
        }
    }

    @Nested
    @DisplayName("퀴즈 삭제")
    class DeleteQuiz {
        @Test
        @DisplayName("성공 - 퀴즈를 삭제하고 세트의 퀴즈 수를 감소시킨다")
        void success() {
            // given
            Long quizId = 100L;
            Quiz quiz = Quiz.builder().quizSet(quizSet).build();
            ReflectionTestUtils.setField(quiz, "id", quizId);
            ReflectionTestUtils.setField(quizSet, "totalQuizCount", 5);

            given(quizRepository.findById(quizId)).willReturn(Optional.of(quiz));

            // when
            quizService.deleteQuiz(quizSetId, quizId, userId);

            // then
            assertThat(quizSet.getTotalQuizCount()).isEqualTo(4);
            verify(quizRepository, times(1)).delete(quiz);
        }
    }
}
