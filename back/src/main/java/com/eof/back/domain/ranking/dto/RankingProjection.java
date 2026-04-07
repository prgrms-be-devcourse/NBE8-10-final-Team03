package com.eof.back.domain.ranking.dto;

import com.eof.back.domain.user.user.entity.User;

/**
 * 기간별 랭킹 조회 쿼리 결과를 담는 Projection DTO입니다.
 * <p>
 * {@code GameRecord}를 기간별로 집계한 결과를 매핑하며,
 * {@code gameRecordRepository.findRankingByPeriod()} 쿼리의 반환 타입으로 사용됩니다.
 * {@code Object[]} 캐스팅 없이 타입 안전하게 결과를 조회하기 위해 도입되었습니다.
 *
 * @param user        랭킹 집계 대상 유저
 * @param periodScore 해당 기간 내 획득한 랭킹 점수 합산
 *
 * @author Jaewon Ryu
 * @since 2026-04-07
 */
public record RankingProjection(User user, Long periodScore) {}