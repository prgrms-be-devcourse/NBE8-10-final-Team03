package com.eof.back.domain.quizset.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetCreateResponse;
import com.eof.back.domain.quizset.service.QuizSetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QuizSetController.class)
class QuizSetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private QuizSetService quizSetService;

    @Test
    @DisplayName("퀴즈 세트 생성 API 호출 성공")
    void createQuizSet_ApiSuccess() throws Exception {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("API 테스트 퀴즈 세트")
                .description("API 설명")
                .totalQuestionCount(5)
                .build();

        QuizSetCreateResponse response = QuizSetCreateResponse.builder()
                .id(1L)
                .title(request.getTitle())
                .description(request.getDescription())
                .creatorNickname("작성자")
                .totalQuestionCount(5)
                .createdAt(LocalDateTime.now())
                .build();

        given(quizSetService.createQuizSet(any(QuizSetCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.title").value("API 테스트 퀴즈 세트"))
                .andExpect(jsonPath("$.data.totalQuestionCount").value(5))
                .andExpect(jsonPath("$.data.creatorNickname").value("작성자"));
    }

    @Test
    @DisplayName("퀴즈 세트 생성 실패 - 제목 누락")
    void createQuizSet_Fail_BlankTitle() throws Exception {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("")
                .description("API 설명")
                .totalQuestionCount(5)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"));
    }

    @Test
    @DisplayName("퀴즈 세트 생성 실패 - 음수 문제 수")
    void createQuizSet_Fail_NegativeCount() throws Exception {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("제목")
                .description("API 설명")
                .totalQuestionCount(-1)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"));
    }
}
