package com.eof.back.domain.quizreport.service;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import com.eof.back.domain.quizreport.entity.QuizReport;
import com.eof.back.domain.quizreport.entity.QuizReportStatus;
import com.eof.back.domain.quizreport.repository.QuizReportRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizReportErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizReportException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class QuizReportServiceTest {

    @Mock
    private QuizReportRepository quizReportRepository;

    @Mock
    private QuizSetRepository quizSetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuizReportServiceImpl quizReportService;

    private User user;
    private User admin;
    private QuizSet quizSet;
    private QuizReport quizReport;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("user")
                .nickname("user")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        admin = User.builder()
                .username("admin")
                .nickname("admin")
                .role(Role.ADMIN)
                .build();
        ReflectionTestUtils.setField(admin, "id", 2L);

        quizSet = QuizSet.builder()
                .title("테스트 퀴즈 세트")
                .build();
        ReflectionTestUtils.setField(quizSet, "id", 10L);

        quizReport = QuizReport.of(user, quizSet, "신고 사유");
        ReflectionTestUtils.setField(quizReport, "id", 100L);
    }

    @Nested
    @DisplayName("신고 생성")
    class CreateReport {
        @Test
        @DisplayName("성공 - 사용자가 신고를 생성한다")
        void success() {
            // given
            QuizReportCreateRequest request = new QuizReportCreateRequest(10L, "사유");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(quizSetRepository.findById(10L)).willReturn(Optional.of(quizSet));
            given(quizReportRepository.save(any(QuizReport.class))).willReturn(quizReport);

            // when
            Long reportId = quizReportService.createReport(request, 1L);

            // then
            assertThat(reportId).isEqualTo(100L);
            verify(quizReportRepository, times(1)).save(any(QuizReport.class));
        }
    }

    @Nested
    @DisplayName("신고 상세 조회")
    class GetReport {
        @Test
        @DisplayName("성공 - 관리자가 신고 상세 내용을 조회한다")
        void success() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));
            given(quizReportRepository.findById(100L)).willReturn(Optional.of(quizReport));

            // when
            QuizReportResponse response = quizReportService.getReport(100L, 2L);

            // then
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getReason()).isEqualTo("신고 사유");
        }

        @Test
        @DisplayName("실패 - 일반 사용자는 조회할 수 없다")
        void fail_notAdmin() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> quizReportService.getReport(100L, 1L))
                    .isInstanceOf(AuthException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_AUTH_FAIL);
        }
    }

    @Nested
    @DisplayName("전체 신고 목록 조회")
    class GetAllReports {
        @Test
        @DisplayName("성공 - 관리자가 모든 신고 목록을 조회한다")
        void success() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));
            given(quizReportRepository.findAll()).willReturn(List.of(quizReport));

            // when
            List<QuizReportResponse> responses = quizReportService.getAllReports(2L);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("신고 처리 완료")
    class ProcessReport {
        @Test
        @DisplayName("성공 - 관리자가 신고를 처리 완료 상태로 변경한다")
        void success() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));
            given(quizReportRepository.findById(100L)).willReturn(Optional.of(quizReport));

            // when
            quizReportService.processReport(100L, 2L);

            // then
            assertThat(quizReport.getStatus()).isEqualTo(QuizReportStatus.PROCESSED);
        }

        @Test
        @DisplayName("실패 - 이미 처리된 신고는 다시 처리할 수 없다")
        void fail_alreadyProcessed() {
            // given
            quizReport.process(); // 이미 PROCESSED 상태
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));
            given(quizReportRepository.findById(100L)).willReturn(Optional.of(quizReport));

            // when & then
            assertThatThrownBy(() -> quizReportService.processReport(100L, 2L))
                    .isInstanceOf(QuizReportException.class)
                    .hasFieldOrPropertyWithValue("errorCode", QuizReportErrorCode.QUIZ_REPORT_ALREADY_PROCESSED);
        }
    }

    @Nested
    @DisplayName("신고 삭제")
    class DeleteReport {
        @Test
        @DisplayName("성공 - 관리자가 신고 내역을 삭제한다")
        void success() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));
            given(quizReportRepository.existsById(100L)).willReturn(true);

            // when
            quizReportService.deleteReport(100L, 2L);

            // then
            verify(quizReportRepository, times(1)).deleteById(100L);
        }
    }
}
