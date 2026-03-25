package com.eof.back.domain.user.quizsetbookmark.controller;

import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkCreateResponse;
import com.eof.back.domain.user.quizsetbookmark.service.QuizSetBookmarkService;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.jwt.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(QuizSetBookmarkControllerTest.MockSecurityConfig.class)
@WebMvcTest(QuizSetBookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizSetBookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizSetBookmarkService quizSetBookmarkService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("북마크 생성 성공")
    void createBookmark_success() throws Exception {
        Long userId = 1L;
        Long quizSetId = 10L;

        given(quizSetBookmarkService.createBookmark(userId, quizSetId))
                .willReturn(new BookmarkCreateResponse(100L, quizSetId));

        mockMvc.perform(post("/api/v1/users/me/bookmarks")
                        .param("quizSetId", quizSetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarkId").value(100))
                .andExpect(jsonPath("$.data.quizSetId").value(quizSetId))
                .andExpect(jsonPath("$.message").value("퀴즈셋 북마크가 추가되었습니다."));
    }

    @Test
    @DisplayName("북마크 제거 성공")
    void deleteBookmark_success() throws Exception {
        Long userId = 1L;
        Long quizSetId = 10L;

        willDoNothing().given(quizSetBookmarkService).deleteBookmark(userId, quizSetId);

        mockMvc.perform(delete("/api/v1/users/me/bookmarks/{quizSetId}", quizSetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("퀴즈셋 북마크가 제거되었습니다."));
    }

    @TestConfiguration
    static class MockSecurityConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(UserPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {
                    return new UserPrincipal(1L, "testUser");
                }
            });
        }
    }
}
