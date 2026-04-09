package com.eof.back.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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
        QuizSetUpdateRequest request = new QuizSetUpdateRequest("수정 제목", "설명", null);

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
    @DisplayName("퀴즈 세트 삭제 성공")
    void deleteQuizSet_Success() {
        // given
        Long quizSetId = 100L;
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizSetRepository.existsById(quizSetId)).willReturn(true);

        // when
        adminService.deleteQuizSet(quizSetId, adminId);

        // then
        verify(quizSetBookmarkRepository).deleteByQuizSetId(quizSetId);
        verify(gameRecordRepository).deleteByQuizSetId(quizSetId);
        verify(gameSessionRepository).deleteByQuizSetId(quizSetId);
        verify(quizRepository).deleteByQuizSetId(quizSetId);
        verify(quizSetRepository).deleteById(quizSetId);
    }

    @Test
    @DisplayName("퀴즈 리포트 처리 성공")
    void processReport_Success() {
        // given
        Long reportId = 50L;
        QuizReport report = mock(QuizReport.class);
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizReportRepository.findById(reportId)).willReturn(Optional.of(report));
        given(report.getStatus()).willReturn(com.eof.back.domain.quizreport.entity.QuizReportStatus.PENDING);

        // when
        adminService.processReport(reportId, adminId);

        // then
        verify(report).process();
    }

    @Test
    @DisplayName("사용자 목록 조회 성공")
    void getUsers_Success() {
        // given
        String keyword = "test";
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Slice<User> slice = new org.springframework.data.domain.SliceImpl<>(java.util.List.of(user));
        
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findByNicknameContaining(keyword, pageable)).willReturn(slice);

        // when
        var response = adminService.getUsers(keyword, pageable, adminId);

        // then
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("사용자 목록 조회 성공 - 키워드 없음")
    void getUsers_NoKeyword_Success() {
        // given
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Slice<User> slice = new org.springframework.data.domain.SliceImpl<>(java.util.List.of(user));

        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findAllBy(pageable)).willReturn(slice);

        // when
        var response = adminService.getUsers(null, pageable, adminId);

        // then
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findAllBy(pageable);
    }

    @Test
    @DisplayName("퀴즈 수정 성공")
    void updateQuiz_Success() {
        // given
        Long quizSetId = 100L;
        Long quizId = 200L;
        QuizSet quizSet = mock(QuizSet.class);
        given(quizSet.getId()).willReturn(quizSetId);

        Quiz quiz = mock(Quiz.class);
        given(quiz.getQuizSet()).willReturn(quizSet);
        given(quiz.getId()).willReturn(quizId);

        com.eof.back.domain.quiz.dto.QuizUpdateRequest request = new com.eof.back.domain.quiz.dto.QuizUpdateRequest(
                com.eof.back.domain.quiz.entity.QuestionType.TEXT,
                com.eof.back.domain.quiz.entity.AnswerType.SHORT_ANSWER,
                "내용", "정답", null, null, null, null, null, null, null, null
        );

        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizRepository.findById(quizId)).willReturn(Optional.of(quiz));

        // when
        adminService.updateQuiz(quizSetId, quizId, request, adminId);

        // then
        verify(quiz).update(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("퀴즈 삭제 성공")
    void deleteQuiz_Success() {
        // given
        Long quizSetId = 100L;
        Long quizId = 200L;
        QuizSet quizSet = mock(QuizSet.class);
        given(quizSet.getId()).willReturn(quizSetId);

        Quiz quiz = mock(Quiz.class);
        given(quiz.getQuizSet()).willReturn(quizSet);

        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizRepository.findById(quizId)).willReturn(Optional.of(quiz));

        // when
        adminService.deleteQuiz(quizSetId, quizId, adminId);

        // then
        verify(quizRepository).delete(quiz);
        verify(quizSet).decreaseQuizCount();
    }

    @Test
    @DisplayName("퀴즈 삭제 실패 - 경로 불일치 (다른 퀴즈 세트의 퀴즈)")
    void deleteQuiz_Fail_PathInconsistency() {
        // given
        Long quizSetId = 100L;
        Long otherQuizSetId = 999L;
        Long quizId = 200L;
        
        QuizSet quizSet = mock(QuizSet.class);
        given(quizSet.getId()).willReturn(otherQuizSetId); // 다른 세트 ID
        
        Quiz quiz = mock(Quiz.class);
        given(quiz.getQuizSet()).willReturn(quizSet);
        
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizRepository.findById(quizId)).willReturn(Optional.of(quiz));

        // when & then
        assertThatThrownBy(() -> adminService.deleteQuiz(quizSetId, quizId, adminId))
                .isInstanceOf(com.eof.back.global.exception.exceptions.QuizException.class);
    }

    @Test
    @DisplayName("특정 신고 내역 조회 성공")
    void getReport_Success() {
        // given
        Long reportId = 50L;
        QuizReport report = mock(QuizReport.class);
        QuizSet mockQuizSet = mock(QuizSet.class);
        User mockReporter = mock(User.class);
        
        given(report.getId()).willReturn(reportId);
        given(report.getReason()).willReturn("사유");
        given(report.getStatus()).willReturn(com.eof.back.domain.quizreport.entity.QuizReportStatus.PENDING);
        given(report.getQuizSet()).willReturn(mockQuizSet);
        given(report.getReporter()).willReturn(mockReporter);
        given(mockQuizSet.getId()).willReturn(200L);
        given(mockReporter.getNickname()).willReturn("reporter");
        
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizReportRepository.findById(reportId)).willReturn(Optional.of(report));

        // when
        var response = adminService.getReport(reportId, adminId);

        // then
        assertThat(response).isNotNull();
        verify(quizReportRepository).findById(reportId);
    }

    @Test
    @DisplayName("특정 신고 내역 조회 실패 - 신고 내역 없음")
    void getReport_Fail_NotFound() {
        // given
        Long reportId = 50L;
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizReportRepository.findById(reportId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.getReport(reportId, adminId))
                .isInstanceOf(com.eof.back.global.exception.exceptions.QuizReportException.class);
    }

    @Test
    @DisplayName("전체 신고 내역 조회 성공")
    void getAllReports_Success() {
        // given
        QuizReport report = mock(QuizReport.class);
        QuizSet mockQuizSet = mock(QuizSet.class);
        User mockReporter = mock(User.class);

        given(report.getId()).willReturn(50L);
        given(report.getReason()).willReturn("사유");
        given(report.getStatus()).willReturn(com.eof.back.domain.quizreport.entity.QuizReportStatus.PENDING);
        given(report.getQuizSet()).willReturn(mockQuizSet);
        given(report.getReporter()).willReturn(mockReporter);
        given(mockQuizSet.getId()).willReturn(200L);
        given(mockReporter.getNickname()).willReturn("reporter");

        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizReportRepository.findAll()).willReturn(java.util.List.of(report));

        // when
        var response = adminService.getAllReports(adminId);

        // then
        assertThat(response).hasSize(1);
        verify(quizReportRepository).findAll();
    }

    @Test
    @DisplayName("신고 내역 삭제 성공")
    void deleteReport_Success() {
        // given
        Long reportId = 50L;
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizReportRepository.existsById(reportId)).willReturn(true);

        // when
        adminService.deleteReport(reportId, adminId);

        // then
        verify(quizReportRepository).deleteById(reportId);
    }

    @Test
    @DisplayName("신고 내역 삭제 실패 - 신고 내역 없음")
    void deleteReport_Fail_NotFound() {
        // given
        Long reportId = 50L;
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizReportRepository.existsById(reportId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> adminService.deleteReport(reportId, adminId))
                .isInstanceOf(com.eof.back.global.exception.exceptions.QuizReportException.class);
    }

    @Test
    @DisplayName("퀴즈 세트 삭제 실패 - 퀴즈 세트 없음")
    void deleteQuizSet_Fail_NotFound() {
        // given
        Long quizSetId = 100L;
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizSetRepository.existsById(quizSetId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> adminService.deleteQuizSet(quizSetId, adminId))
                .isInstanceOf(com.eof.back.global.exception.exceptions.QuizSetException.class);
    }

    @Test
    @DisplayName("퀴즈 리포트 처리 실패 - 이미 처리된 신고")
    void processReport_Fail_AlreadyProcessed() {
        // given
        Long reportId = 50L;
        QuizReport report = mock(QuizReport.class);
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(quizReportRepository.findById(reportId)).willReturn(Optional.of(report));
        given(report.getStatus()).willReturn(com.eof.back.domain.quizreport.entity.QuizReportStatus.PROCESSED);

        // when & then
        assertThatThrownBy(() -> adminService.processReport(reportId, adminId))
                .isInstanceOf(com.eof.back.global.exception.exceptions.QuizReportException.class);
    }

    @Test
    @DisplayName("사용자 정지 실패 - 대상 사용자 없음")
    void suspendUser_Fail_UserNotFound() {
        // given
        given(userRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.suspendUser(userId, "사유", 7, adminId))
                .isInstanceOf(AuthException.class);
    }
}
