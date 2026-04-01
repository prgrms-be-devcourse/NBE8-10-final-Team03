package com.eof.back.domain.quizreport.service;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.entity.QuizReport;
import com.eof.back.domain.quizreport.repository.QuizReportRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
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
}
