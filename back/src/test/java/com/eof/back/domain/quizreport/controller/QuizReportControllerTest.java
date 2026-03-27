package com.eof.back.domain.quizreport.controller;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import com.eof.back.domain.quizreport.entity.QuizReportStatus;
import com.eof.back.domain.quizreport.service.QuizReportService;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.jwt.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizReportController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private QuizReportService quizReportService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new UserPrincipal(1L, "testuser", "tester", "ADMIN");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("신고 생성 API 호출 성공")
    void createReport_success() throws Exception {
        // given
        QuizReportCreateRequest request = new QuizReportCreateRequest(1L, "오타가 있어요.");
        given(quizReportService.createReport(any(), any())).willReturn(100L);

        // when & then
        mockMvc.perform(post("/api/v1/reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/reports/100"))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @DisplayName("신고 상세 조회 API 호출 성공")
    void getReport_success() throws Exception {
        // given
        Long reportId = 100L;
        QuizReportResponse response = QuizReportResponse.builder()
                .id(reportId)
                .quizSetId(1L)
                .quizSetTitle("퀴즈셋 제목")
                .reporterNickname("tester")
                .reason("신고 사유")
                .status(QuizReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        given(quizReportService.getReport(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/reports/{id}", reportId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.reason").value("신고 사유"));
    }

    @Test
    @DisplayName("전체 신고 목록 조회 API 호출 성공")
    void getAllReports_success() throws Exception {
        // given
        QuizReportResponse response = QuizReportResponse.builder()
                .id(1L)
                .reason("사유1")
                .status(QuizReportStatus.PENDING)
                .build();
        given(quizReportService.getAllReports(any())).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/reports"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].reason").value("사유1"));
    }

    @Test
    @DisplayName("신고 처리 완료 API 호출 성공")
    void processReport_success() throws Exception {
        // given
        Long reportId = 100L;
        doNothing().when(quizReportService).processReport(any(), any());

        // when & then
        mockMvc.perform(patch("/api/v1/reports/{id}/process", reportId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("신고 처리가 완료되었습니다."));
    }

    @Test
    @DisplayName("신고 삭제 API 호출 성공")
    void deleteReport_success() throws Exception {
        // given
        Long reportId = 100L;
        doNothing().when(quizReportService).deleteReport(any(), any());

        // when & then
        mockMvc.perform(delete("/api/v1/reports/{id}", reportId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
