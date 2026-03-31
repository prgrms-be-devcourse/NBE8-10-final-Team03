package com.eof.back.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eof.back.admin.entity.UserSuspension;
import com.eof.back.admin.repository.UserSuspensionRepository;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizreport.entity.QuizReport;
import com.eof.back.domain.quizreport.repository.QuizReportRepository;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.quizsetbookmark.repository.QuizSetBookmarkRepository;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.entity.UserStatus;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.exceptions.AuthException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminServiceImpl adminService;

    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizSetRepository quizSetRepository;
    @Mock
    private QuizReportRepository quizReportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSuspensionRepository userSuspensionRepository;
    @Mock
    private QuizSetBookmarkRepository quizSetBookmarkRepository;
    @Mock
    private GameRecordRepository gameRecordRepository;
    @Mock
    private GameSessionRepository gameSessionRepository;

    private User admin;
    private User user;
    private Long adminId = 1L;
    private Long userId = 2L;

    @BeforeEach
    void setUp() {
        admin = User.builder().nickname("admin").role(Role.ADMIN).build();
        ReflectionTestUtils.setField(admin, "id", adminId);

        user = User.builder().nickname("user").role(Role.USER).build();
        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    @DisplayName("관리자 권한으로 퀴즈 세트 수정 성공")
    void updateQuizSet_Success() {
        // given
        QuizSet quizSet = QuizSet.builder().title("기본 제목").build();
        ReflectionTestUtils.setField(quizSet, "id", 100L);
        QuizSetUpdateRequest request = new QuizSetUpdateRequest("수정 제목", "설명");

        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizSetRepository.findById(100L)).willReturn(Optional.of(quizSet));

        // when
        adminService.updateQuizSet(100L, request, adminId);

        // then
        assertThat(quizSet.getTitle()).isEqualTo("수정 제목");
    }

    @Test
    @DisplayName("사용자 정지 성공")
    void suspendUser_Success() {
        // given
        int days = 7;
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userSuspensionRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when
        adminService.suspendUser(userId, "부적절한 언어", days, adminId);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        verify(userSuspensionRepository).save(any(UserSuspension.class));
    }

    @Test
    @DisplayName("사용자 삭제 처리 성공")
    void deleteUser_Success() {
        // given
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        adminService.deleteUser(userId, adminId);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userSuspensionRepository).deleteByUserId(userId);
    }
}
