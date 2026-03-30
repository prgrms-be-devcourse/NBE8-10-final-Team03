package com.eof.back.domain.user.gamerecord.controller;

import com.eof.back.domain.user.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.user.gamerecord.service.RecordService;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(RecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecordService recordService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Test
    @DisplayName("내 전적 조회 - 정상")
    void getMyRecords_success() throws Exception {
        UserRecordResponse response = new UserRecordResponse(
                10, 3, 1500L, List.of(), 0, 10, 10
        );
        given(recordService.getMyRecords(eq(1L), eq(0), eq(10))).willReturn(response);

        mockMvc.perform(get("/api/v1/users/1/records")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalGames").value(10));
    }

    @Test
    @DisplayName("내 전적 조회 - 음수 page 보정")
    void getMyRecords_negativePage() throws Exception {
        UserRecordResponse response = new UserRecordResponse(
                0, 0, 0L, List.of(), 0, 10, 0
        );
        given(recordService.getMyRecords(1L, 0, 10)).willReturn(response);

        mockMvc.perform(get("/api/v1/users/1/records")
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
        given(recordService.getMyRecords(any(), eq(0), eq(10))).willReturn(response);

        mockMvc.perform(get("/api/v1/users/1/records")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isOk());
    }
}