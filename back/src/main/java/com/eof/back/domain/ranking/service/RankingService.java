package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponse;

/**
 * 랭킹 조회 기능을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 현재 MySQL 기반으로 구현되어 있으며,
 * 추후 Redis 등으로 구현체를 교체할 수 있습니다.
 *
 * @author Jaewon Ryu
 * @see RankingServiceImpl
 * @since 2026-03-23
 */
public interface RankingService {
    RankingResponse getTopRankings();
}