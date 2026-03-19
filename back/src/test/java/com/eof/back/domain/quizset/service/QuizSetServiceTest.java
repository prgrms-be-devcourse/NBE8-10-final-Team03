package com.eof.back.domain.quizset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetCreateResponse;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.entity.Role;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
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
                .totalQuestionCount(10)
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
                .totalQuizCount(request.getTotalQuestionCount())
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);

        given(userRepository.findById(1L)).willReturn(Optional.of(creator));
        given(quizSetRepository.save(any(QuizSet.class))).willReturn(quizSet);

        // when
        QuizSetCreateResponse response = quizSetService.createQuizSet(request);

        // then
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("테스트 퀴즈 세트");
        assertThat(response.getCreatorNickname()).isEqualTo("별명");
        assertThat(response.getTotalQuestionCount()).isEqualTo(10);
        
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
                .totalQuestionCount(10)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> quizSetService.createQuizSet(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("임시 사용자(ID: 1)를 찾을 수 없습니다.");
        
        verify(userRepository).findById(1L);
    }
}
