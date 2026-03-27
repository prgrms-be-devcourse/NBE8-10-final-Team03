package com.eof.back.domain.user.quizsetbookmark.controller;

import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkItemResponse;
import com.eof.back.domain.user.quizsetbookmark.service.QuizSetBookmarkService;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptionHadler.DefaultExceptionHandler;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(DefaultExceptionHandler.class)
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

    @Nested
    @DisplayName("createBookmark")
    class CreateBookmark {

        @Test
        @DisplayName("성공 - 북마크를 생성하고 201을 반환한다")
        void success() throws Exception {
            Long quizSetId = 10L;

            willDoNothing().given(quizSetBookmarkService).createBookmark(1L, quizSetId);

            mockMvc.perform(post("/api/v1/users/1/bookmarks")
                            .param("quizSetId", quizSetId.toString()))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("deleteBookmark")
    class DeleteBookmark {

        @Test
        @DisplayName("성공 - 북마크를 삭제하고 204를 반환한다")
        void success() throws Exception {
            Long quizSetId = 10L;

            willDoNothing().given(quizSetBookmarkService).deleteBookmark(1L, quizSetId);

            mockMvc.perform(delete("/api/v1/users/1/bookmarks/{quizSetId}", quizSetId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 북마크하지 않은 퀴즈셋이면 404를 반환한다")
        void fail_notFound() throws Exception {
            Long quizSetId = 10L;

            willThrow(new QuizSetException(QuizSetErrorCode.QUIZ_SET_BOOKMARK_NOT_FOUND))
                    .given(quizSetBookmarkService).deleteBookmark(1L, quizSetId);

            mockMvc.perform(delete("/api/v1/users/1/bookmarks/{quizSetId}", quizSetId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("북마크하지 않은 퀴즈 세트입니다."));
        }
    }

    @Nested
    @DisplayName("getBookmarks")
    class GetBookmarks {

        @Test
        @DisplayName("성공 - 북마크 목록을 반환한다")
        void success() throws Exception {
            Long userId = 1L;

            given(quizSetBookmarkService.getBookmarks(userId))
                    .willReturn(List.of(
                            new BookmarkItemResponse(10L, "퀴즈셋1", "설명1", 5),
                            new BookmarkItemResponse(20L, "퀴즈셋2", "설명2", 10)
                    ));

            mockMvc.perform(get("/api/v1/users/1/bookmarks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].quizSetId").value(10))
                    .andExpect(jsonPath("$.data[0].title").value("퀴즈셋1"))
                    .andExpect(jsonPath("$.data[0].totalQuizCount").value(5))
                    .andExpect(jsonPath("$.data[1].quizSetId").value(20))
                    .andExpect(jsonPath("$.data[1].title").value("퀴즈셋2"))
                    .andExpect(jsonPath("$.data[1].totalQuizCount").value(10))
                    .andExpect(jsonPath("$.message").value("내 퀴즈셋 북마크 목록을 조회했습니다."));
        }

        @Test
        @DisplayName("성공 - 북마크가 없으면 빈 배열을 반환한다")
        void success_empty() throws Exception {
            given(quizSetBookmarkService.getBookmarks(1L)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/users/1/bookmarks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("내 퀴즈셋 북마크 목록을 조회했습니다."));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자면 404를 반환한다")
        void fail_userNotFound() throws Exception {
            given(quizSetBookmarkService.getBookmarks(1L))
                    .willThrow(new AuthException(AuthErrorCode.USER_NOT_FOUND));

            mockMvc.perform(get("/api/v1/users/1/bookmarks"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."));
        }
    }

}
