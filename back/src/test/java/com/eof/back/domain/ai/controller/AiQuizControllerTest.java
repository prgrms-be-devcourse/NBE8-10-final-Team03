package com.eof.back.domain.ai.controller;

import com.eof.back.domain.ai.dto.AiQuizGenerateResponse;
import com.eof.back.domain.ai.service.AiQuizService;
import com.eof.back.domain.auth.service.CaptchaService;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.QuizSetException;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.jwt.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Import(AiQuizControllerTest.MockSecurityConfig.class)
@WebMvcTest(AiQuizController.class)
class AiQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiQuizService aiQuizService;

    @MockitoBean
    private CaptchaService captchaService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @WithMockUser
    @DisplayName("AI 퀴즈 생성 - 정상")
    void generateQuiz_success() throws Exception {
        given(aiQuizService.generateQuiz(anyString(), any()))
                .willReturn(new AiQuizGenerateResponse(1L));

        mockMvc.perform(post("/api/v1/ai/quizzes")
                        .with(csrf())
                        .param("topic", "한국사"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quizSetId").value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("AI 퀴즈 생성 - 빈 topic")
    void generateQuiz_emptyTopic() throws Exception {
        mockMvc.perform(post("/api/v1/ai/quizzes")
                        .with(csrf())
                        .param("topic", ""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    @DisplayName("AI 퀴즈 생성 - topic 없음")
    void generateQuiz_missingTopic() throws Exception {
        mockMvc.perform(post("/api/v1/ai/quizzes")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("AI 퀴즈 생성 - 부적절한 주제 예외")
    void generateQuiz_inappropriateTopic() throws Exception {
        given(aiQuizService.generateQuiz(anyString(), any()))
                .willThrow(new QuizSetException(QuizSetErrorCode.INVALID_TOPIC));

        mockMvc.perform(post("/api/v1/ai/quizzes")
                        .with(csrf())
                        .param("topic", "욕설"))
                .andExpect(status().isBadRequest());
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class MockSecurityConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(java.util.List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.getParameterType().equals(UserPrincipal.class);
                }

                @Override
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                              org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                              org.springframework.web.context.request.NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    return new UserPrincipal(1L, "tester", "tester", "USER");
                }
            });
        }
    }
}