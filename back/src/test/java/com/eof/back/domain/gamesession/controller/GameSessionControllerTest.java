package com.eof.back.domain.gamesession.controller;

import com.eof.back.domain.gamesession.dto.*;
import com.eof.back.domain.gamesession.entity.GameSessionStatus;
import com.eof.back.domain.gamesession.service.GameSessionService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(GameSessionControllerTest.MockSecurityConfig.class)
@WebMvcTest(GameSessionController.class)
public class GameSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private GameSessionService gameSessionService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser
    @DisplayName("게임 세션 생성 성공 테스트")
    void createGameSession_Success() throws Exception {
        GameSessionCreateRequest request = new GameSessionCreateRequest("테스트 방", 10L, 4, 10);

        GameSessionCreateResponse response = GameSessionCreateResponse.builder()
                .gameSessionId(1L)
                .roomName("테스트 방")
                .hostUserId(1L)
                .quizSetId(10L)
                .maxPlayers(4)
                .status(GameSessionStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .maxQuizzes(10)
                .build();

        // 서비스 모킹 (eq(1L)로 명확하게 매칭)
        given(gameSessionService.createGameSession(eq(1L), any(GameSessionCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/rooms/1"))
                .andExpect(jsonPath("$.message").value("방 생성이 완료되었습니다."))
                .andExpect(jsonPath("$.data.roomName").value("테스트 방"));
    }

    @Test
    @WithMockUser
    @DisplayName("전체 게임 세션 조회 테스트")
    void getAllGameSession_Success() throws Exception {
        GameSessionListResponse session1 = GameSessionListResponse.builder().gameSessionId(1L).roomName("방1").build();
        GameSessionListResponse session2 = GameSessionListResponse.builder().gameSessionId(2L).roomName("방2").build();

        given(gameSessionService.getAllGameSessions()).willReturn(List.of(session1, session2));

        mockMvc.perform(get("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].roomName").value("방1"));
    }

    @Test
    @WithMockUser
    @DisplayName("방 이름으로 게임 세션 검색 테스트")
    void searchGameSessions_Success() throws Exception {
        String searchKeyword = "상식";
        GameSessionListResponse session = GameSessionListResponse.builder().gameSessionId(10L).roomName("상식 배틀방").build();

        given(gameSessionService.getGameSessionByRoomName(searchKeyword)).willReturn(List.of(session));

        mockMvc.perform(get("/api/v1/rooms/search")
                        .param("roomName", searchKeyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roomName").value("상식 배틀방"));
    }

    @Test
    @WithMockUser
    @DisplayName("게임 세션 삭제 성공 테스트")
    void deleteGameSession_Success() throws Exception {
        Long gameSessionId = 10L;

        mockMvc.perform(delete("/api/v1/rooms/{gameSessionId}", gameSessionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(gameSessionService).deleteGameSession(eq(1L), eq(gameSessionId));
    }

    @Test
    @WithMockUser
    @DisplayName("게임 세션 입장 API 테스트")
    void joinGameSession_Success() throws Exception {
        Long gameSessionId = 10L;

        GameSessionJoinResponse mockResponse = new GameSessionJoinResponse(
                gameSessionId, "테스트 방", 100L, "WAIT",
                List.of(new GameSessionJoinResponse.PlayerInfo(1L, "테스터", true))
        );

        given(gameSessionService.joinRoom(eq(1L), eq(gameSessionId))).willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/rooms/{gameSessionId}/join", gameSessionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("방에 입장하셨습니다."))
                .andExpect(jsonPath("$.data.gameSessionId").value(gameSessionId));
    }

    @Test
    @WithMockUser
    @DisplayName("게임 세션 퇴장 API 테스트")
    void leaveGameSession_Success() throws Exception {
        Long gameSessionId = 10L;

        mockMvc.perform(delete("/api/v1/rooms/{gameSessionId}/leave", gameSessionId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(gameSessionService, times(1)).leaveRoom(eq(1L), eq(gameSessionId));
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
                    return new UserPrincipal(1L, "tester");
                }
            });
        }
    }
}