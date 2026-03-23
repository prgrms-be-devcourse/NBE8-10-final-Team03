package com.eof.back.domain.ranking.controller;

import com.eof.back.domain.ranking.dto.RankingResponseDto;
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

import java.util.List;

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

    @BeforeEach
    void setUpAuth() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("testuser", null, List.of());
        auth.setDetails(1L);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("랭킹 조회 - 정상")
    void getTopRankings_success() throws Exception {
        RankingResponseDto response = new RankingResponseDto(List.of(
                new RankingResponseDto.RankingItem(1, "유저1", 5000L),
                new RankingResponseDto.RankingItem(2, "유저2", 3000L)
        ));
        given(rankingService.getTopRankings()).willReturn(response);

        mockMvc.perform(get("/api/v1/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.rankings").isArray())
                .andExpect(jsonPath("$.data.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.data.rankings[0].nickname").value("유저1"))
                .andExpect(jsonPath("$.data.rankings[0].totalRankingScore").value(5000));
    }

    @Test
    @DisplayName("랭킹 조회 - 빈 결과")
    void getTopRankings_empty() throws Exception {
        given(rankingService.getTopRankings()).willReturn(new RankingResponseDto(List.of()));

        mockMvc.perform(get("/api/v1/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.rankings").isEmpty());
    }
}