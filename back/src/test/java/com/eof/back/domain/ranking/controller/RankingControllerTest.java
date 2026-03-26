package com.eof.back.domain.ranking.controller;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.ranking.service.RankingService;
import com.eof.back.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.eof.back.global.jwt.UserPrincipal;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RankingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final UserPrincipal principal = new UserPrincipal(1L, "testuser", "tester", "USER");

    @BeforeEach
    void setUpAuth() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("전체 랭킹 조회 - 정상")
    void getTopRankings_success() throws Exception {
        RankingResponse response = new RankingResponse(
                3L,
                List.of(
                        new RankingResponse.RankingItem(1, "유저1", 5000L),
                        new RankingResponse.RankingItem(2, "유저2", 3000L)
                )
        );
        given(rankingService.getTopRankings(any())).willReturn(response);

        mockMvc.perform(get("/api/v1/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.myRank").value(3))
                .andExpect(jsonPath("$.data.rankings").isArray())
                .andExpect(jsonPath("$.data.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.data.rankings[0].nickname").value("유저1"))
                .andExpect(jsonPath("$.data.rankings[0].score").value(5000));
    }

    @Test
    @DisplayName("전체 랭킹 조회 - 빈 결과")
    void getTopRankings_empty() throws Exception {
        given(rankingService.getTopRankings(any()))
                .willReturn(new RankingResponse(null, List.of()));

        mockMvc.perform(get("/api/v1/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.rankings").isEmpty());
    }

    @Test
    @DisplayName("주간 랭킹 조회 - 정상")
    void getWeeklyRankings_success() throws Exception {
        RankingResponse response = new RankingResponse(
                2L,
                List.of(
                        new RankingResponse.RankingItem(1, "유저1", 1500L)
                )
        );
        given(rankingService.getWeeklyRankings(any())).willReturn(response);

        mockMvc.perform(get("/api/v1/rankings").param("period", "weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myRank").value(2))
                .andExpect(jsonPath("$.data.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.data.rankings[0].score").value(1500));
    }

    @Test
    @DisplayName("월간 랭킹 조회 - 정상")
    void getMonthlyRankings_success() throws Exception {
        RankingResponse response = new RankingResponse(
                5L,
                List.of(
                        new RankingResponse.RankingItem(1, "유저1", 8000L)
                )
        );
        given(rankingService.getMonthlyRankings(any())).willReturn(response);

        mockMvc.perform(get("/api/v1/rankings").param("period", "monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myRank").value(5))
                .andExpect(jsonPath("$.data.rankings[0].score").value(8000));
    }

    @Test
    @DisplayName("비로그인 랭킹 조회 - myRank null")
    void getTopRankings_notLoggedIn() throws Exception {
        SecurityContextHolder.clearContext(); // 비로그인 상태
        given(rankingService.getTopRankings(null))
                .willReturn(new RankingResponse(null, List.of(
                        new RankingResponse.RankingItem(1, "유저1", 5000L)
                )));

        mockMvc.perform(get("/api/v1/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myRank").doesNotExist())
                .andExpect(jsonPath("$.data.rankings[0].rank").value(1));
    }
}