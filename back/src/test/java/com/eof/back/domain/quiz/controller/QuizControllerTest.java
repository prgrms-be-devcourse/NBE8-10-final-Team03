package com.eof.back.domain.quiz.controller;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.service.QuizService;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser
    @DisplayName("퀴즈 생성 API 호출 성공")
    void createQuiz_success() throws Exception {
        // given
        Long quizSetId = 1L;
        QuizCreateRequest request = new QuizCreateRequest(
                "문제 내용", "정답", "보기1", "보기2", "보기3", "보기4"
        );
        given(quizService.createQuiz(eq(quizSetId), any(QuizCreateRequest.class))).willReturn(100L);

        // when & then
        mockMvc.perform(post("/api/v1/quizsets/{quizSetId}/quizzes", quizSetId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/quizzes/100"));
    }

    @Test
    @WithMockUser
    @DisplayName("퀴즈 생성 실패 - 필수 값 누락")
    void createQuiz_fail_invalidRequest() throws Exception {
        // given
        Long quizSetId = 1L;
        QuizCreateRequest request = new QuizCreateRequest(
                "", "정답", "보기1", "보기2", "보기3", "보기4" // content is empty
        );

        // when & then
        mockMvc.perform(post("/api/v1/quizsets/{quizSetId}/quizzes", quizSetId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
