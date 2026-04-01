package com.eof.back.domain.ranking.controller;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.ranking.service.RankingService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.eof.back.global.jwt.UserPrincipal;

/**
 * 랭킹 조회 API를 처리하는 REST 컨트롤러입니다.
 * <p>
 * {@code /api/v1/rankings} 경로로 들어오는 요청을 처리하며,
 * {@link RankingService}를 통해 상위 랭킹 데이터를 조회하여 반환합니다.
 * <p>
 * 비로그인 사용자도 접근 가능하며, 로그인 시 내 순위가 함께 반환됩니다.
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
     * 랭킹을 조회합니다.
     * <p>
     * period 파라미터로 전체/주간/월간을 구분합니다.
     * 비로그인 시 myRank는 null로 반환됩니다.
     *
     * @param period 조회 기간 (weekly, monthly, 없으면 전체)
     * @param principal 로그인한 유저 정보 (비로그인 시 null)
     * @return 랭킹 조회 결과 (순위, 닉네임, 점수, 내 순위)
     */
    @GetMapping
    public ResponseEntity<Response<RankingResponse>> getRankings(
            @RequestParam(required = false) String period,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal != null ? principal.id() : null;

        RankingResponse response = switch (period != null ? period : "all") {
            case "weekly"  -> rankingService.getWeeklyRankings(userId);
            case "monthly" -> rankingService.getMonthlyRankings(userId);
            default        -> rankingService.getTopRankings(userId);
        };

        return ResponseEntity.ok(
                CommonResponse.success(response, "랭킹 조회 성공")
        );
    }
}