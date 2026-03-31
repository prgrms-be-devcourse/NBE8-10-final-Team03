package com.eof.back.domain.quizset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.quizsetbookmark.repository.QuizSetBookmarkRepository;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.QuizSetException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class QuizSetServiceTest {

    @InjectMocks
    private QuizSetServiceImpl quizSetService;

    @Mock
    private QuizSetRepository quizSetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizSetBookmarkRepository quizSetBookmarkRepository;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Test
    @DisplayName("퀴즈 세트 생성 성공")
    void createQuizSet_Success() {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("테스트 퀴즈 세트")
                .description("설명")
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
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);

        given(userRepository.findById(1L)).willReturn(Optional.of(creator));
        given(quizSetRepository.save(any(QuizSet.class))).willReturn(quizSet);

        // when
        Long responseId = quizSetService.createQuizSet(request, 1L);

        // then
        assertThat(responseId).isEqualTo(100L);
        assertThat(quizSet.getTotalQuizCount()).isEqualTo(0);
        
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
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 1L);

        Quiz quiz = Quiz.builder()
                .content("문제 내용")
                .answer("정답")
                .choice1("1")
                .choice2("2")
                .choice3("3")
                .choice4("4")
                .build();
        ReflectionTestUtils.setField(quiz, "id", 10L);
        quizSet.getQuizzes().add(quiz);
        quizSet.increaseQuizCount();

        given(quizSetRepository.findById(1L)).willReturn(Optional.of(quizSet));

        // when
        QuizSetResponse response = quizSetService.getQuizSet(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getQuizzes()).hasSize(1);
        assertThat(response.getQuizzes().get(0).content()).isEqualTo("문제 내용");
        assertThat(response.getTotalQuizCount()).isEqualTo(1);
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
        QuizSet quizSet1 = QuizSet.builder().title("세트1").creator(creator).build();
        QuizSet quizSet2 = QuizSet.builder().title("세트2").creator(creator).build();

        PageRequest pageable = PageRequest.of(0, 12);
        Slice<QuizSet> slice = new SliceImpl<>(List.of(quizSet1, quizSet2), pageable, false);

        given(quizSetRepository.findAllWithCreator(any(Pageable.class))).willReturn(slice);

        // when
        Slice<QuizSetListResponse> responses = quizSetService.getAllQuizSets(pageable);

        // then
        assertThat(responses.getContent()).hasSize(2);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("세트1");
        assertThat(responses.getContent().get(1).getTitle()).isEqualTo("세트2");
        assertThat(responses.hasNext()).isFalse();

        verify(quizSetRepository).findAllWithCreator(pageable);
    }

    @Test
    @DisplayName("퀴즈 세트 수정 성공")
    void updateQuizSet_Success() {
        // given
        User creator = User.builder().nickname("별명").build();
        ReflectionTestUtils.setField(creator, "id", 1L);
        QuizSet quizSet = QuizSet.builder()
                .title("기존 제목")
                .description("기존 설명")
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);

        QuizSetUpdateRequest request = new QuizSetUpdateRequest("수정 제목", "수정 설명");

        given(quizSetRepository.findById(100L)).willReturn(Optional.of(quizSet));

        // when
        Long updatedId = quizSetService.updateQuizSet(100L, request, 1L);

        // then
        assertThat(updatedId).isEqualTo(100L);
        assertThat(quizSet.getTitle()).isEqualTo("수정 제목");
        assertThat(quizSet.getDescription()).isEqualTo("수정 설명");
    }

    @Test
    @DisplayName("퀴즈 세트 삭제 성공 - 모든 연관 엔티티에 대해 명시적 Bulk Delete 수행")
    void deleteQuizSet_Success() {
        // given
        Long quizSetId = 100L;
        Long userId = 1L;

        given(quizSetRepository.deleteByIdAndCreatorId(quizSetId, userId)).willReturn(1);

        // when
        quizSetService.deleteQuizSet(quizSetId, userId);

        // then
        verify(quizSetBookmarkRepository).deleteByQuizSetId(quizSetId);
        verify(gameRecordRepository).deleteByQuizSetId(quizSetId);
        verify(gameSessionRepository).deleteByQuizSetId(quizSetId);
        verify(quizRepository).deleteByQuizSetId(quizSetId);
        verify(quizSetRepository).deleteByIdAndCreatorId(quizSetId, userId);
    }

    @Test
    @DisplayName("퀴즈 세트 삭제 실패 - 존재하지 않는 경우")
    void deleteQuizSet_Fail_NotFound() {
        // given
        Long quizSetId = 100L;
        Long userId = 1L;

        given(quizSetRepository.deleteByIdAndCreatorId(quizSetId, userId)).willReturn(0);
        given(quizSetRepository.existsById(quizSetId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> quizSetService.deleteQuizSet(quizSetId, userId))
                .isInstanceOf(QuizSetException.class)
                .hasFieldOrPropertyWithValue("errorCode", QuizSetErrorCode.QUIZ_SET_NOT_FOUND);
    }

    @Test
    @DisplayName("퀴즈 세트 삭제 실패 - 권한이 없는 경우")
    void deleteQuizSet_Fail_AccessDenied() {
        // given
        Long quizSetId = 100L;
        Long userId = 1L;

        given(quizSetRepository.deleteByIdAndCreatorId(quizSetId, userId)).willReturn(0);
        given(quizSetRepository.existsById(quizSetId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> quizSetService.deleteQuizSet(quizSetId, userId))
                .isInstanceOf(QuizSetException.class)
                .hasFieldOrPropertyWithValue("errorCode", QuizSetErrorCode.QUIZ_SET_ACCESS_DENIED);
    }
}
