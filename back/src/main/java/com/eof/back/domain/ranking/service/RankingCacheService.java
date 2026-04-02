package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 랭킹 캐시 조회를 담당하는 서비스 클래스입니다.
 * <p>
 * {@code @Cacheable}을 통해 전체/주간/월간 랭킹 TOP10을 Redis에 캐싱합니다.
 * 동일 클래스 내 호출 시 AOP 프록시를 타지 않는 문제를 해결하기 위해
 * {@link RankingServiceImpl}에서 분리된 별도 빈입니다.
 *
 * <p><b>캐시 목록:</b><br>
 * {@code ranking:all} - 전체 랭킹 TOP10 <br>
 * {@code ranking:weekly} - 주간 랭킹 TOP10 <br>
 * {@code ranking:monthly} - 월간 랭킹 TOP10 <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service}로 등록되며, 생성자 주입을 통해 의존성을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @see RankingServiceImpl
 * @since 2026-04-01
 */
@Service
@RequiredArgsConstructor
public class RankingCacheService {

    private final UserRepository userRepository;
    private final GameRecordRepository gameRecordRepository;

    @Cacheable(value = "ranking:all", key = "'top10'")
    @Transactional(readOnly = true)
    public List<RankingResponse.RankingItem> getTopRankingItems() {
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
    public List<RankingResponse.RankingItem> getWeeklyRankingItems() {
        LocalDateTime since = LocalDateTime.now().minusWeeks(1);
        return getPeriodRankingItems(since);
    }

    @Cacheable(value = "ranking:monthly", key = "'top10'")
    @Transactional(readOnly = true)
    public List<RankingResponse.RankingItem> getMonthlyRankingItems() {
        LocalDateTime since = LocalDateTime.now().minusMonths(1);
        return getPeriodRankingItems(since);
    }

    private List<RankingResponse.RankingItem> getPeriodRankingItems(LocalDateTime since) {
        List<Object[]> results = gameRecordRepository.findRankingByPeriod(since, PageRequest.of(0, 10));
        List<RankingResponse.RankingItem> rankings = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            String nickname = ((User) row[0]).getNickname();
            Long score = (Long) row[1];
            rankings.add(new RankingResponse.RankingItem(i + 1, nickname, score));
        }
        return rankings;
    }
}