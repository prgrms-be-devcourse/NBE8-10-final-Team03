package com.eof.back.domain.quizset.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.domain.quizset.service.QuizSetService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.QuizSetException;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(QuizSetController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizSetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private QuizSetService quizSetService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private CookieUtil cookieUtil;

    private UserPrincipal principal;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        principal = new UserPrincipal(1L, "testuser", "tester", "USER");
        adminPrincipal = new UserPrincipal(2L, "admin", "admin", "ADMIN");
    }

    @Test
    @DisplayName("퀴즈 세트 생성 API 호출 성공")
    void createQuizSet_ApiSuccess() throws Exception {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("API 테스트 퀴즈 세트")
                .description("API 설명")
                .build();

        given(quizSetService.createQuizSet(any(), any())).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of())
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/quizsets/1"));
    }

    @Test
    @DisplayName("퀴즈 세트 생성 실패 - 제목 누락")
    void createQuizSet_Fail_BlankTitle() throws Exception {
        // given
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("")
                .description("API 설명")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of())
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"));
    }

    @Test
    @DisplayName("퀴즈 세트 생성 실패 - 설명 255자 초과")
    void createQuizSet_Fail_TooLongDescription() throws Exception {
        // given
        String longDescription = "a".repeat(256);
        QuizSetCreateRequest request = QuizSetCreateRequest.builder()
                .title("제목")
                .description(longDescription)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/quizsets")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of())
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"));
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 API 호출 성공 - 작성자 본인")
    void getQuizSet_ApiSuccess_Author() throws Exception {
        // given
        QuizSetResponse response = QuizSetResponse.builder()
                .id(1L)
                .title("테스트 세트")
                .quizzes(List.of())
                .build();

        given(quizSetService.getQuizSet(anyLong(), anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/1")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of())
                        )))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 API 호출 성공 - 관리자")
    void getQuizSet_ApiSuccess_Admin() throws Exception {
        // given
        QuizSetResponse response = QuizSetResponse.builder()
                .id(1L)
                .title("테스트 세트")
                .quizzes(List.of())
                .build();

        given(quizSetService.getQuizSet(anyLong(), anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/1")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(adminPrincipal, null, List.of())
                        )))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("퀴즈 세트 단건 조회 API 호출 실패 - 권한 없음")
    void getQuizSet_ApiFail_Forbidden() throws Exception {
        // given
        given(quizSetService.getQuizSet(anyLong(), anyLong()))
                .willThrow(new QuizSetException(QuizSetErrorCode.QUIZ_SET_ACCESS_DENIED));

        // when & then
        mockMvc.perform(get("/api/v1/quizsets/1")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of())
                        )))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("퀴즈 세트 전체 목록 조회 API 호출 성공")
    void getAllQuizSets_ApiSuccess() throws Exception {
        // given
        QuizSetListResponse response1 = QuizSetListResponse.builder().id(1L).title("세트1").build();
        Pageable pageable = PageRequest.of(0, 12);
        Slice<QuizSetListResponse> slice = new SliceImpl<>(List.of(response1), pageable, false);

        given(quizSetService.getAllQuizSets(any(Pageable.class))).willReturn(slice);

        // when & then
        mockMvc.perform(get("/api/v1/quizsets"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("퀴즈 세트 수정 API 호출 성공")
    void updateQuizSet_ApiSuccess() throws Exception {
        // given
        Long quizSetId = 1L;
        QuizSetUpdateRequest request = new QuizSetUpdateRequest("수정된 제목", "수정된 설명");

        given(quizSetService.updateQuizSet(anyLong(), any(), anyLong())).willReturn(quizSetId);

        // when & then
        mockMvc.perform(patch("/api/v1/quizsets/{id}", quizSetId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of())
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(quizSetId));
    }

    @Test
    @DisplayName("퀴즈 세트 삭제 API 호출 성공")
    void deleteQuizSet_ApiSuccess() throws Exception {
        // given
        Long quizSetId = 1L;
        willDoNothing().given(quizSetService).deleteQuizSet(anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/quizsets/{id}", quizSetId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(principal, null, List.of())
                        )))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
