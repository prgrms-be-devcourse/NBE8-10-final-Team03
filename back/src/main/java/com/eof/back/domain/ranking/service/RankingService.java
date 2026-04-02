package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponse;

import java.util.List;

/**
 * 랭킹 조회 기능을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 전체/주간/월간 랭킹 TOP10 조회 및 캐시 조회 메서드를 제공합니다.
 * {@link RankingCacheService}와 협력하여 Redis 캐싱을 통해 DB 부하를 최소화합니다.
 *
 * @author Jaewon Ryu
 * @see RankingServiceImpl
 * @see RankingCacheService
 * @since 2026-04-02
 */
public interface RankingService {

    RankingResponse getTopRankings(Long userId);

    RankingResponse getWeeklyRankings(Long userId);

    RankingResponse getMonthlyRankings(Long userId);

    List<RankingResponse.RankingItem> getCachedTopRankings();

    List<RankingResponse.RankingItem> getCachedWeeklyRankings();

    List<RankingResponse.RankingItem> getCachedMonthlyRankings();
}