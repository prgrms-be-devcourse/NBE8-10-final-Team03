package com.eof.back.domain.quizset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.quizsetbookmark.repository.QuizSetBookmarkRepository;
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
                .title(request.title())
                .description(request.description())
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);

        given(userRepository.findById(1L)).willReturn(Optional.of(creator));
        given(quizSetRepository.save(any(QuizSet.class))).willReturn(quizSet);

        // when
        Long responseId = quizSetService.createQuizSet(request, 1L);

        // then
        assertThat(responseId).isEqualTo(100L);
        
        verify(userRepository).findById(1L);
        verify(quizSetRepository).save(any(QuizSet.class));
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 성공 - 작성자 본인인 경우")
    void getQuizSet_Success() {
        // given
        User creator = User.builder().nickname("별명").role(Role.USER).build();
        ReflectionTestUtils.setField(creator, "id", 1L);
        QuizSet quizSet = QuizSet.builder()
                .title("테스트 세트")
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 1L);

        given(quizSetRepository.findById(1L)).willReturn(Optional.of(quizSet));
        given(userRepository.findById(1L)).willReturn(Optional.of(creator));

        // when
        QuizSetResponse response = quizSetService.getQuizSet(1L, 1L);

        // then
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 성공 - 관리자인 경우")
    void getQuizSet_Success_Admin() {
        // given
        User creator = User.builder().nickname("작성자").role(Role.USER).build();
        ReflectionTestUtils.setField(creator, "id", 1L);
        
        User admin = User.builder().nickname("관리자").role(Role.ADMIN).build();
        ReflectionTestUtils.setField(admin, "id", 2L);

        QuizSet quizSet = QuizSet.builder()
                .title("테스트 세트")
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 1L);

        given(quizSetRepository.findById(1L)).willReturn(Optional.of(quizSet));
        given(userRepository.findById(2L)).willReturn(Optional.of(admin));

        // when
        QuizSetResponse response = quizSetService.getQuizSet(1L, 2L);

        // then
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 실패 - 작성자도 아니고 관리자도 아닌 경우")
    void getQuizSet_Fail_AccessDenied() {
        // given
        User creator = User.builder().nickname("작성자").role(Role.USER).build();
        ReflectionTestUtils.setField(creator, "id", 1L);
        
        User otherUser = User.builder().nickname("타인").role(Role.USER).build();
        ReflectionTestUtils.setField(otherUser, "id", 3L);

        QuizSet quizSet = QuizSet.builder()
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 1L);

        given(quizSetRepository.findById(1L)).willReturn(Optional.of(quizSet));
        given(userRepository.findById(3L)).willReturn(Optional.of(otherUser));

        // when & then
        assertThatThrownBy(() -> quizSetService.getQuizSet(1L, 3L))
                .isInstanceOf(QuizSetException.class)
                .hasFieldOrPropertyWithValue("errorCode", QuizSetErrorCode.QUIZ_SET_ACCESS_DENIED);
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
        verify(quizSetRepository).findAllWithCreator(pageable);
    }

    @Test
    @DisplayName("퀴즈 세트 수정 성공")
    void updateQuizSet_Success() {
        // given
        User creator = User.builder().nickname("별명").role(Role.USER).build();
        ReflectionTestUtils.setField(creator, "id", 1L);
        QuizSet quizSet = QuizSet.builder()
                .title("기존 제목")
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
    }

    @Test
    @DisplayName("퀴즈 세트 삭제 성공")
    void deleteQuizSet_Success() {
        // given
        Long quizSetId = 100L;
        Long userId = 1L;

        given(quizSetRepository.deleteByIdAndCreatorId(quizSetId, userId)).willReturn(1);

        // when
        quizSetService.deleteQuizSet(quizSetId, userId);

        // then
        verify(quizSetRepository).deleteByIdAndCreatorId(quizSetId, userId);
    }
}
