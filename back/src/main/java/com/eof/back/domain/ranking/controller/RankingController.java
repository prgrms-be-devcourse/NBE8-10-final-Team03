package com.eof.back.domain.ranking.controller;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.ranking.service.RankingService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 랭킹 조회 API를 처리하는 REST 컨트롤러입니다.
 * <p>
 * {@code /api/v1/rankings} 경로로 들어오는 요청을 처리하며,
 * {@link RankingService}를 통해 상위 랭킹 데이터를 조회하여 반환합니다.
 * <p>
 * 비로그인 사용자도 접근 가능합니다.
 *
 * @author Jaewon Ryu
 * @see RankingService
 * @see RankingResponse
 * @since 2026-03-23
 */
@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 상위 10명의 랭킹을 조회합니다.
     *
     * @return 랭킹 조회 결과 (순위, 닉네임, 점수)
     */
    @GetMapping
    public ResponseEntity<Response<RankingResponse>> getTopRankings() {
        return ResponseEntity.ok(
                CommonResponse.success(
                        rankingService.getTopRankings(),
                        "랭킹 조회 성공"
                )
        );
    }
}