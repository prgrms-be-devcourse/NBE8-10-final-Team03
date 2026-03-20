package com.eof.back.domain.gamerecord.controller;

import com.eof.back.domain.gamerecord.controller.RecordController;
import com.eof.back.domain.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.gamerecord.service.RecordService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code RecordControllerTest(String example)} <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author Jaewon Ryu
 * @since 2026-03-20
 * @see
 */
@WebMvcTest(RecordController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화 (JWT 아직 없으니까)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecordService recordService;

    @Test
    @DisplayName("내 전적 조회 - 정상")
    void getMyRecords_success() throws Exception {
        // given
        UserRecordResponse response = new UserRecordResponse(
                10, 3, 1500L,
                List.of(),
                0, 10, 10
        );

        given(recordService.getMyRecords(1L, 0, 10)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/me/records")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalGames").value(10))
                .andExpect(jsonPath("$.data.totalWins").value(3))
                .andExpect(jsonPath("$.data.totalRankingScore").value(1500));
    }

    @Test
    @DisplayName("내 전적 조회 - 기본 페이지 파라미터")
    void getMyRecords_defaultParams() throws Exception {
        // given
        UserRecordResponse response = new UserRecordResponse(
                0, 0, 0L,
                List.of(),
                0, 10, 0
        );

        given(recordService.getMyRecords(1L, 0, 10)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/me/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
}