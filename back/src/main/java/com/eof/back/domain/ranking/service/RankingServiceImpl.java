package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 랭킹 조회 기능의 구현체입니다.
 * <p>
 * {@link RankingCacheService}를 통해 Redis에 캐싱된 TOP10 랭킹을 조회하고,
 * 내 순위({@code myRank})는 항상 DB에서 실시간으로 조회합니다.
 *
 * <p><b>캐싱 전략:</b><br>
 * TOP10은 {@link RankingCacheService}에서 캐싱 처리하며,
 * 동일 클래스 내 호출 시 AOP 프록시를 타지 않는 문제를 해결하기 위해
 * 캐시 로직을 별도 빈으로 분리하였습니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service}로 등록되며, 생성자 주입을 통해 의존성을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @see RankingService
 * @see RankingCacheService
 * @since 2026-03-23
 */
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final UserRepository userRepository;
    private final GameRecordRepository gameRecordRepository;
    private final RankingCacheService rankingCacheService;

    /**
     * 상위 10명의 랭킹을 조회합니다.
     * <p>
     * User 테이블의 totalRankingScore를 기준으로
     * 내림차순 정렬하여 상위 10명을 반환합니다.
     *
     * @return 상위 10명의 랭킹 정보 (순위, 닉네임, 점수)
     */
    @Override
    @Transactional(readOnly = true)
    public RankingResponse getTopRankings(Long userId) {
        List<RankingResponse.RankingItem> rankings = rankingCacheService.getTopRankingItems();
        Long myRank = getMyRank(userId);
        return new RankingResponse(myRank, rankings);
    }
    /**
     * 주간 TOP 10 랭킹과 내 순위를 반환합니다.
     * <p>
     * 7일 이내의 GameRecord.earnedRankingScore를 유저별로 집계합니다.
     *
     * @param userId 로그인한 유저 ID (비로그인 시 null)
     * @return 주간 랭킹 응답
     */
    @Override
    @Transactional(readOnly = true)
    public RankingResponse getWeeklyRankings(Long userId) {
        return new RankingResponse(null, rankingCacheService.getWeeklyRankingItems());
    }
    /**
     * 월간 TOP 10 랭킹과 내 순위를 반환합니다.
     * <p>
     * 30일 이내의 GameRecord.earnedRankingScore를 유저별로 집계합니다.
     *
     * @param userId 로그인한 유저 ID (비로그인 시 null)
     * @return 월간 랭킹 응답
     */
    @Override
    @Transactional(readOnly = true)
    public RankingResponse getMonthlyRankings(Long userId) {
        return new RankingResponse(null, rankingCacheService.getMonthlyRankingItems());
    }




    @Cacheable(value = "ranking:all", key = "'top10'")
    @Transactional(readOnly = true)
    public List<RankingResponse.RankingItem> getCachedTopRankings() {
        List<User> topUsers = userRepository.findTop10ActiveUsers(PageRequest.of(0, 10));
        List<RankingResponse.RankingItem> rankings = new ArrayList<>();
        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);
            rankings.add(new RankingResponse.RankingItem(i + 1, user.getNickname(), user.getTotalRankingScore()));
        }
        return rankings;
    }

    @Cacheable(value = "ranking:weekly", key = "'top10'")
    @Transactional(readOnly = true)
    public List<RankingResponse.RankingItem> getCachedWeeklyRankings() {
        LocalDateTime since = LocalDateTime.now().minusWeeks(1);
        return getPeriodRankingItems(since);
    }

    @Cacheable(value = "ranking:monthly", key = "'top10'")
    @Transactional(readOnly = true)
    public List<RankingResponse.RankingItem> getCachedMonthlyRankings() {
        LocalDateTime since = LocalDateTime.now().minusMonths(1);
        return getPeriodRankingItems(since);
    }

    private List<RankingResponse.RankingItem> getPeriodRankingItems(LocalDateTime since) {
        List<Object[]> results = gameRecordRepository.findRankingByPeriod(since, PageRequest.of(0, 10));
        List<RankingResponse.RankingItem> rankings = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            User user = (User) results.get(i)[0];
            Long score = (Long) results.get(i)[1];
            rankings.add(new RankingResponse.RankingItem(i + 1, user.getNickname(), score));
        }
        return rankings;
    }
    private Long getMyRank(Long userId) {
        if (userId == null) return null;
        return userRepository.findMyRankByUserId(userId);
    }
}