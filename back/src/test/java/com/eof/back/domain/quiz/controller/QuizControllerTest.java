package com.eof.back.domain.quiz.controller;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quiz.entity.AnswerType;
import com.eof.back.domain.quiz.entity.QuestionType;
import com.eof.back.domain.quiz.service.QuizService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @MockitoBean
    private CookieUtil cookieUtil;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new UserPrincipal(1L, "testuser", "tester", "USER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("퀴즈 생성 API 호출 성공")
    void createQuiz_success() throws Exception {
        // given
        Long quizSetId = 1L;
        QuizCreateRequest request = new QuizCreateRequest(
                QuestionType.TEXT, AnswerType.MULTIPLE_CHOICE, "문제 내용", "정답", null, null, "보기1", "보기2", "보기3", "보기4"
        );

        given(quizService.createQuiz(any(), any(), any())).willReturn(100L);

        // when & then
        mockMvc.perform(post("/api/v1/quizsets/{quizSetId}/quizzes", quizSetId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/quizsets/1/quizzes/100"));
    }

    @Test
    @DisplayName("퀴즈 생성 실패 - 필수 값 누락")
    void createQuiz_fail_invalidRequest() throws Exception {
        // given
        Long quizSetId = 1L;
        QuizCreateRequest request = new QuizCreateRequest(
                null, null, "", "정답", null, null, "보기1", "보기2", "보기3", "보기4"
        );

        // when & then
        mockMvc.perform(post("/api/v1/quizsets/{quizSetId}/quizzes", quizSetId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("퀴즈 목록 조회 API 호출 성공")
    void getQuizzesByQuizSetId_success() throws Exception {
        // given
        Long quizSetId = 1L;
        QuizResponse quiz1 = QuizResponse.builder().id(10L).questionType(QuestionType.TEXT).answerType(AnswerType.MULTIPLE_CHOICE).content("문제1").build();
        QuizResponse quiz2 = QuizResponse.builder().id(11L).questionType(QuestionType.TEXT).answerType(AnswerType.MULTIPLE_CHOICE).content("문제2").build();
        given(quizService.getQuizzesByQuizSetId(quizSetId)).willReturn(List.of(quiz1, quiz2));

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/{quizSetId}/quizzes", quizSetId))
                .andDo(print())
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
        QuizResponse response = QuizResponse.builder().id(quizId).questionType(QuestionType.TEXT).answerType(AnswerType.MULTIPLE_CHOICE).content("문제 내용").build();
        given(quizService.getQuiz(quizSetId, quizId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/{quizSetId}/quizzes/{quizId}", quizSetId, quizId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(quizId))
                .andExpect(jsonPath("$.data.content").value("문제 내용"));
    }

    @Test
    @DisplayName("퀴즈 수정 API 호출 성공")
    void updateQuiz_success() throws Exception {
        // given
        Long quizSetId = 1L;
        Long quizId = 100L;
        QuizUpdateRequest request = new QuizUpdateRequest(null, null, "수정된 내용", null, null, null, null, null, null, null);

        given(quizService.updateQuiz(any(), any(), any(), any())).willReturn(quizId);

        // when & then
        mockMvc.perform(patch("/api/v1/quizsets/{quizSetId}/quizzes/{quizId}", quizSetId, quizId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(quizId));
    }

    @Test
    @DisplayName("퀴즈 삭제 API 호출 성공")
    void deleteQuiz_success() throws Exception {
        // given
        Long quizSetId = 1L;
        Long quizId = 100L;

        doNothing().when(quizService).deleteQuiz(quizSetId, quizId, 1L);

        // when & then
        mockMvc.perform(delete("/api/v1/quizsets/{quizSetId}/quizzes/{quizId}", quizSetId, quizId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
