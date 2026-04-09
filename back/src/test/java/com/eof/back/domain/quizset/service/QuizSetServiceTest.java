package com.eof.back.domain.quizset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.image.service.ImageService;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetInfoResponse;
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

    @Mock
    private ImageService imageService;

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
    @DisplayName("퀴즈 세트 기본 정보 조회 성공")
    void getQuizSetInfo_Success() {
        // given
        User creator = User.builder().nickname("별명").role(Role.USER).build();
        QuizSet quizSet = QuizSet.builder()
                .title("테스트 세트")
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 1L);

        given(quizSetRepository.findById(1L)).willReturn(Optional.of(quizSet));

        // when
        QuizSetInfoResponse response = quizSetService.getQuizSetInfo(1L);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("테스트 세트");
        verify(quizSetRepository).findById(1L);
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

        QuizSetUpdateRequest request = new QuizSetUpdateRequest("수정 제목", "수정 설명", null);

        given(quizSetRepository.findById(100L)).willReturn(Optional.of(quizSet));

        // when
        Long updatedId = quizSetService.updateQuizSet(100L, request, 1L);

        // then
        assertThat(updatedId).isEqualTo(100L);
        assertThat(quizSet.getTitle()).isEqualTo("수정 제목");
    }

    @Test
    @DisplayName("퀴즈 세트 수정 성공 - 썸네일 삭제 (thumbnailUrl 빈 문자열)")
    void updateQuizSet_ThumbnailDelete_Success() {
        // given
        User creator = User.builder().nickname("별명").role(Role.USER).build();
        ReflectionTestUtils.setField(creator, "id", 1L);
        QuizSet quizSet = QuizSet.builder()
                .title("기존 제목")
                .thumbnailUrl("https://s3.example.com/old-thumb.jpg")
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);

        QuizSetUpdateRequest request = new QuizSetUpdateRequest("기존 제목", null, "");

        given(quizSetRepository.findById(100L)).willReturn(Optional.of(quizSet));
        willDoNothing().given(imageService).deleteImage(anyString(), eq(1L));

        // when
        quizSetService.updateQuizSet(100L, request, 1L);

        // then
        verify(imageService).deleteImage("https://s3.example.com/old-thumb.jpg", 1L);
        assertThat(quizSet.getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("퀴즈 세트 수정 성공 - thumbnailUrl null이면 기존 썸네일 유지")
    void updateQuizSet_ThumbnailNotChanged_WhenNull() {
        // given
        User creator = User.builder().nickname("별명").role(Role.USER).build();
        ReflectionTestUtils.setField(creator, "id", 1L);
        QuizSet quizSet = QuizSet.builder()
                .title("기존 제목")
                .thumbnailUrl("https://s3.example.com/old-thumb.jpg")
                .creator(creator)
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);

        QuizSetUpdateRequest request = new QuizSetUpdateRequest("수정 제목", null, null);

        given(quizSetRepository.findById(100L)).willReturn(Optional.of(quizSet));

        // when
        quizSetService.updateQuizSet(100L, request, 1L);

        // then
        verify(imageService, never()).deleteImage(anyString(), any());
        assertThat(quizSet.getThumbnailUrl()).isEqualTo("https://s3.example.com/old-thumb.jpg");
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

    @Test
    @DisplayName("퀴즈 세트 삭제 실패 - 존재하지 않는 퀴즈 세트")
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
    @DisplayName("퀴즈 세트 삭제 실패 - 작성자가 아님")
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

    @Test
    @DisplayName("퀴즈 세트 수정 실패 - 작성자가 아님")
    void updateQuizSet_Fail_NotCreator() {
        // given
        User creator = User.builder().nickname("작성자").build();
        ReflectionTestUtils.setField(creator, "id", 1L);
        QuizSet quizSet = QuizSet.builder().creator(creator).build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);

        QuizSetUpdateRequest request = new QuizSetUpdateRequest("제목", null, null);

        given(quizSetRepository.findById(100L)).willReturn(Optional.of(quizSet));

        // when & then
        assertThatThrownBy(() -> quizSetService.updateQuizSet(100L, request, 2L)) // 다른 유저 ID
                .isInstanceOf(QuizSetException.class)
                .hasFieldOrPropertyWithValue("errorCode", QuizSetErrorCode.QUIZ_SET_ACCESS_DENIED);
    }
}
