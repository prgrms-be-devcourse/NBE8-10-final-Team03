package com.eof.back.domain.quiz.controller;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.service.QuizService;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                .andExpect(header().string("Location", "/api/v1/quizsets/1/quizzes/100"));
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

    @Test
    @DisplayName("퀴즈 목록 조회 API 호출 성공")
    void getQuizzesByQuizSetId_success() throws Exception {
        // given
        Long quizSetId = 1L;
        QuizResponse quiz1 = QuizResponse.builder().id(10L).content("문제1").build();
        QuizResponse quiz2 = QuizResponse.builder().id(11L).content("문제2").build();
        given(quizService.getQuizzesByQuizSetId(quizSetId)).willReturn(List.of(quiz1, quiz2));

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/{quizSetId}/quizzes", quizSetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[1].id").value(11));
    }

    @Test
    @DisplayName("퀴즈 단건 조회 API 호출 성공")
    void getQuiz_success() throws Exception {
        // given
        Long quizSetId = 1L;
        Long quizId = 100L;
        QuizResponse response = QuizResponse.builder().id(quizId).content("문제 내용").build();
        given(quizService.getQuiz(quizId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/{quizSetId}/quizzes/{quizId}", quizSetId, quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(quizId))
                .andExpect(jsonPath("$.data.content").value("문제 내용"));
    }
}
