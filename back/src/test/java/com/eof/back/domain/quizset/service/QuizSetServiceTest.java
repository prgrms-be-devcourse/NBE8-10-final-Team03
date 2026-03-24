package com.eof.back.domain.quizset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetCreateResponse;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.entity.Role;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.exceptions.QuizSetException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QuizSetServiceTest {

    @InjectMocks
    private QuizSetServiceImpl quizSetService;

    @Mock
    private QuizSetRepository quizSetRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("퀴즈 세트 생성 성공")
    void createQuizSet_Success() {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("테스트 퀴즈 세트")
                .description("설명")
                .totalQuizCount(10)
                .build();

        User creator = User.builder()
                .username("testuser")
                .password("password")
                .nickname("별명")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(creator, "id", 1L);

        QuizSet quizSet = QuizSet.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .creator(creator)
                .totalQuizCount(request.getTotalQuizCount())
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);

        given(userRepository.findById(1L)).willReturn(Optional.of(creator));
        given(quizSetRepository.save(any(QuizSet.class))).willReturn(quizSet);

        // when
        QuizSetCreateResponse response = quizSetService.createQuizSet(request, 1L);

        // then
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("테스트 퀴즈 세트");
        assertThat(response.getCreatorNickname()).isEqualTo("별명");
        assertThat(response.getTotalQuizCount()).isEqualTo(10);
        
        verify(userRepository).findById(1L);
        verify(quizSetRepository).save(any(QuizSet.class));
    }

    @Test
    @DisplayName("퀴즈 세트 생성 실패 - 제작자를 찾을 수 없음")
    void createQuizSet_Fail_CreatorNotFound() {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("테스트 퀴즈 세트")
                .description("설명")
                .totalQuizCount(10)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> quizSetService.createQuizSet(request, 1L))
                .isInstanceOf(com.eof.back.global.exception.exceptions.AuthException.class)
                .hasMessageContaining("해당 사용자를 찾을 수 없습니다.");
        
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 성공")
    void getQuizSet_Success() {
        // given
        User creator = User.builder().nickname("별명").build();
        QuizSet quizSet = QuizSet.builder()
                .title("테스트 세트")
                .description("설명")
                .creator(creator)
                .totalQuizCount(1)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 1L);

        Quiz quiz = Quiz.builder()
                .content("문제 내용")
                .answer("정답")
                .choice1("1")
                .choice2("2")
                .choice3("3")
                .choice4("4")
                .sequence(1)
                .build();
        ReflectionTestUtils.setField(quiz, "id", 10L);
        quizSet.getQuizzes().add(quiz);

        given(quizSetRepository.findById(1L)).willReturn(Optional.of(quizSet));

        // when
        QuizSetResponse response = quizSetService.getQuizSet(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getQuizzes()).hasSize(1);
        assertThat(response.getQuizzes().get(0).getContent()).isEqualTo("문제 내용");
        verify(quizSetRepository).findById(1L);
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 실패 - 존재하지 않는 식별자")
    void getQuizSet_Fail_NotFound() {
        // given
        given(quizSetRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> quizSetService.getQuizSet(1L))
                .isInstanceOf(QuizSetException.class);
    }

    @Test
    @DisplayName("퀴즈 세트 전체 목록 조회 성공")
    void getAllQuizSets_Success() {
        // given
        User creator = User.builder().nickname("별명").build();
        QuizSet quizSet1 = QuizSet.builder().title("세트1").creator(creator).totalQuizCount(5).build();
        QuizSet quizSet2 = QuizSet.builder().title("세트2").creator(creator).totalQuizCount(10).build();

        given(quizSetRepository.findAll()).willReturn(List.of(quizSet1, quizSet2));

        // when
        List<QuizSetListResponse> responses = quizSetService.getAllQuizSets();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("세트1");
        assertThat(responses.get(1).getTitle()).isEqualTo("세트2");
        verify(quizSetRepository).findAll();
    }
}
