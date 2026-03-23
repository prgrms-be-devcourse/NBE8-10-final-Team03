package com.eof.back.domain.ranking.controller;

import com.eof.back.domain.ranking.dto.RankingResponseDto;
import com.eof.back.domain.ranking.service.RankingService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code RankingController(String example)} <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author Jaewon Ryu
 * @see
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
    public ResponseEntity<Response<RankingResponseDto>> getTopRankings() {
        return ResponseEntity.ok(
                CommonResponse.success(
                        rankingService.getTopRankings(),
                        "랭킹 조회 성공"
                )
        );
    }
}