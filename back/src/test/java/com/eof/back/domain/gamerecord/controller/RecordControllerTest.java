package com.eof.back.domain.gamerecord.controller;

import com.eof.back.domain.gamerecord.controller.RecordController;
import com.eof.back.domain.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.gamerecord.service.RecordService;
import com.eof.back.global.jwt.JwtAuthenticationFilter;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecordService recordService;

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
    @DisplayName("내 전적 조회 - 정상")
    void getMyRecords_success() throws Exception {
        UserRecordResponse response = new UserRecordResponse(
                10, 3, 1500L, List.of(), 0, 10, 10
        );
        given(recordService.getMyRecords(1L, 0, 10)).willReturn(response);

        mockMvc.perform(get("/api/v1/me/records")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalGames").value(10))
                .andExpect(jsonPath("$.data.totalWins").value(3));
    }

    @Test
    @DisplayName("내 전적 조회 - 음수 page 보정")
    void getMyRecords_negativePage() throws Exception {
        UserRecordResponse response = new UserRecordResponse(
                0, 0, 0L, List.of(), 0, 10, 0
        );
        given(recordService.getMyRecords(1L, 0, 10)).willReturn(response);

        mockMvc.perform(get("/api/v1/me/records")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내 전적 조회 - size 0 보정")
    void getMyRecords_zeroSize() throws Exception {
        UserRecordResponse response = new UserRecordResponse(
                0, 0, 0L, List.of(), 0, 10, 0
        );
        given(recordService.getMyRecords(1L, 0, 10)).willReturn(response);

        mockMvc.perform(get("/api/v1/me/records")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isOk());
        verify(recordService).getMyRecords(1L, 0, 10);
    }
}