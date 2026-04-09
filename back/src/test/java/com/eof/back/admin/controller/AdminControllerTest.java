package com.eof.back.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.eof.back.admin.service.AdminService;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quiz.entity.AnswerType;
import com.eof.back.domain.quiz.entity.QuestionType;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.token.TokenVersionStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private TokenVersionStore tokenVersionStore;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private com.eof.back.global.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
    }

/*
    @Test
    @DisplayName("신고 전체 조회 API 호출 성공")
    @WithMockUser(username = "1", roles = "ADMIN")
    void getAllReports_Success() throws Exception {
        // given
        QuizReportResponse report = QuizReportResponse.builder().id(1L).reason("신고 사유").build();
        given(adminService.getAllReports(anyLong())).willReturn(List.of(report));

        // when & then
        mockMvc.perform(get("/api/v1/admin/reports"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }
*/

    @Test
    @DisplayName("특정 신고 상세 조회 API 호출 성공")
    void getReport_Success() throws Exception {
        // given
        QuizReportResponse report = QuizReportResponse.builder().id(1L).reason("신고 사유").build();
        given(adminService.getReport(anyLong(), anyLong())).willReturn(report);

        // when & then
        mockMvc.perform(get("/api/v1/admin/reports/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("신고 처리 완료 API 호출 성공")
    void processReport_Success() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/admin/reports/1/process"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("신고 처리가 완료되었습니다."));
    }

    @Test
    @DisplayName("신고 삭제 API 호출 성공")
    void deleteReport_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/admin/reports/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("퀴즈 세트 수정 API 호출 성공")
    void updateQuizSet_Success() throws Exception {
        // given
        QuizSetUpdateRequest request = new QuizSetUpdateRequest("제목", "설명", "url");
        given(adminService.updateQuizSet(anyLong(), any(), anyLong())).willReturn(100L);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/quizsets/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @DisplayName("퀴즈 세트 삭제 API 호출 성공")
    void deleteQuizSet_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/admin/quizsets/100"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

/*
    @Test
    @DisplayName("퀴즈 수정 API 호출 성공")
    void updateQuiz_Success() throws Exception {
        // given
        QuizUpdateRequest request = new QuizUpdateRequest(
                QuestionType.TEXT, AnswerType.SHORT_ANSWER, "내용", "정답", null, null, null, null, null, null, null, null
        );
        given(adminService.updateQuiz(anyLong(), anyLong(), any(), anyLong())).willReturn(200L);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/quizsets/100/quizzes/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(200));
    }
*/

    @Test
    @DisplayName("퀴즈 삭제 API 호출 성공")
    void deleteQuiz_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/admin/quizsets/100/quizzes/200"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("사용자 목록 조회 API 호출 성공")
    void getUsers_Success() throws Exception {
        // given
        given(adminService.getUsers(any(), any(), anyLong())).willReturn(new SliceImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("사용자 정지 API 호출 성공")
    void suspendUser_Success() throws Exception {
        // given
        AdminController.UserSuspensionRequest request = new AdminController.UserSuspensionRequest(
                "사유", 7
        );

        // when & then
        mockMvc.perform(patch("/api/v1/admin/users/2/suspend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("사용자 정지 API - reason 없으면 400 반환")
    void suspendUser_Fail_BlankReason() throws Exception {
        // given
        AdminController.UserSuspensionRequest request = new AdminController.UserSuspensionRequest(
                "", 7
        );

        // when & then
        mockMvc.perform(patch("/api/v1/admin/users/2/suspend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 정지 API - suspensionDays 0이면 400 반환")
    void suspendUser_Fail_ZeroDays() throws Exception {
        // given
        AdminController.UserSuspensionRequest request = new AdminController.UserSuspensionRequest(
                "사유", 0
        );

        // when & then
        mockMvc.perform(patch("/api/v1/admin/users/2/suspend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 삭제 API 호출 성공")
    void deleteUser_Success() throws Exception {
        // when & then
        mockMvc.perform(patch("/api/v1/admin/users/2/delete"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("사용자가 삭제(탈퇴) 처리되었습니다."));
    }
}
