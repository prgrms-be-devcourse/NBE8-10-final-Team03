package com.eof.back.domain.quizset.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetCreateResponse;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.service.QuizSetService;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.QuizSetException;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String BEARER_TOKEN = "Bearer test-token";

    @BeforeEach
    void setUpJwtMock() {
        Claims mockClaims = Mockito.mock(Claims.class);
        given(jwtTokenProvider.validateToken(anyString())).willReturn(mockClaims);
        given(jwtTokenProvider.getUserId(mockClaims)).willReturn(1L);
        given(jwtTokenProvider.getUsername(mockClaims)).willReturn("testuser");
        given(jwtTokenProvider.getRole(mockClaims)).willReturn("USER");
    }

    @Test
    @DisplayName("퀴즈 세트 생성 API 호출 성공")
    void createQuizSet_ApiSuccess() throws Exception {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("API 테스트 퀴즈 세트")
                .description("API 설명")
                .totalQuizCount(5)
                .build();

        QuizSetCreateResponse response = QuizSetCreateResponse.builder()
                .id(1L)
                .title(request.getTitle())
                .description(request.getDescription())
                .creatorNickname("작성자")
                .totalQuizCount(5)
                .createdAt(LocalDateTime.now())
                .build();

        given(quizSetService.createQuizSet(any(QuizSetCreateRequest.class), any(Long.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.title").value("API 테스트 퀴즈 세트"))
                .andExpect(jsonPath("$.data.totalQuizCount").value(5))
                .andExpect(jsonPath("$.data.creatorNickname").value("작성자"));
    }

    @Test
    @DisplayName("퀴즈 세트 생성 실패 - 제목 누락")
    void createQuizSet_Fail_BlankTitle() throws Exception {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("")
                .description("API 설명")
                .totalQuizCount(5)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .header("Authorization", BEARER_TOKEN)
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
                .totalQuizCount(-1)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"));
    }

    @Test
    @DisplayName("퀴즈 세트 생성 실패 - 설명 1000자 초과")
    void createQuizSet_Fail_TooLongDescription() throws Exception {
        // given
        String longDescription = "a".repeat(1001);
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("제목")
                .description(longDescription)
                .totalQuizCount(5)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("description: 퀴즈 세트 설명은 1000자를 초과할 수 없습니다."));
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 API 호출 성공")
    void getQuizSet_ApiSuccess() throws Exception {
        // given
        QuizSetResponse.QuizResponse quizResponse = QuizSetResponse.QuizResponse.builder()
                .id(1L)
                .content("문제 내용")
                .answer("정답")
                .choice1("1")
                .choice2("2")
                .choice3("3")
                .choice4("4")
                .sequence(1)
                .build();

        QuizSetResponse response = QuizSetResponse.builder()
                .id(1L)
                .title("테스트 세트")
                .description("설명")
                .creatorNickname("작성자")
                .totalQuizCount(1)
                .quizzes(List.of(quizResponse))
                .build();

        given(quizSetService.getQuizSet(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/1")
                        .header("Authorization", BEARER_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("테스트 세트"))
                .andExpect(jsonPath("$.data.quizzes[0].content").value("문제 내용"));
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 실패 - 존재하지 않는 식별자")
    void getQuizSet_ApiFail_NotFound() throws Exception {
        // given
        given(quizSetService.getQuizSet(anyLong()))
                .willThrow(new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/999")
                        .header("Authorization", BEARER_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("해당 퀴즈 세트를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("퀴즈 세트 전체 목록 조회 API 호출 성공")
    void getAllQuizSets_ApiSuccess() throws Exception {
        // given
        QuizSetListResponse response1 = QuizSetListResponse.builder()
                .id(1L)
                .title("세트1")
                .creatorNickname("작성자1")
                .totalQuizCount(5)
                .build();
        QuizSetListResponse response2 = QuizSetListResponse.builder()
                .id(2L)
                .title("세트2")
                .creatorNickname("작성자2")
                .totalQuizCount(10)
                .build();

        given(quizSetService.getAllQuizSets()).willReturn(List.of(response1, response2));

        // when & then
        mockMvc.perform(get("/api/v1/quizsets"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].title").value("세트1"))
                .andExpect(jsonPath("$.data[1].title").value("세트2"));
    }
}
