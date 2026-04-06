package com.eof.back.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eof.back.admin.entity.UserSuspension;
import com.eof.back.admin.repository.UserSuspensionRepository;
import com.eof.back.domain.auth.store.RefreshTokenStore;
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
import com.eof.back.global.token.TokenVersionStore;
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
    @Mock
    private RefreshTokenStore refreshTokenStore;
    @Mock
    private TokenVersionStore tokenVersionStore;

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
    @DisplayName("사용자 정지 성공 - 상태 변경, tokenVersion 증가, refreshToken 삭제")
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
        // 정지 즉시 토큰 무효화: version 증가(access token 차단) + refresh token 삭제
        verify(tokenVersionStore).increment(userId);
        verify(refreshTokenStore).delete(userId);
    }

    @Test
    @DisplayName("사용자 정지 성공 - 기존 정지 이력 있으면 update")
    void suspendUser_UpdateExistingSuspension() {
        // given
        UserSuspension existing = UserSuspension.of(user, "이전 사유", LocalDateTime.now().plusDays(1));
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userSuspensionRepository.findByUserId(userId)).willReturn(Optional.of(existing));

        // when
        adminService.suspendUser(userId, "새 사유", 14, adminId);

        // then
        verify(userSuspensionRepository).findByUserId(userId);
        verify(tokenVersionStore).increment(userId);
        verify(refreshTokenStore).delete(userId);
    }

    @Test
    @DisplayName("사용자 정지 실패 - 권한 없으면 예외 발생")
    void suspendUser_Fail_NotAdmin() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user)); // 일반 유저가 adminId 위치

        // when & then
        assertThatThrownBy(() -> adminService.suspendUser(userId, "사유", 7, userId))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("사용자 삭제 성공 - 상태 변경, tokenVersion 삭제, refreshToken 삭제")
    void deleteUser_Success() {
        // given
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        adminService.deleteUser(userId, adminId);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userSuspensionRepository).deleteByUserId(userId);
        // 삭제 즉시 토큰 완전 무효화: version 키 삭제 + refresh token 삭제
        verify(tokenVersionStore).delete(userId);
        verify(refreshTokenStore).delete(userId);
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 권한 없으면 예외 발생")
    void deleteUser_Fail_NotAdmin() {
        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> adminService.deleteUser(userId, userId))
                .isInstanceOf(AuthException.class);
    }
}
