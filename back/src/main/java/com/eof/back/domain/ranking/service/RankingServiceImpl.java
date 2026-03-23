package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 랭킹 조회 기능의 MySQL 기반 구현체입니다.
 * <p>
 * {@link UserRepository}를 통해 User 테이블의
 * totalRankingScore 기준 상위 10명을 조회합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service}로 등록되며, 생성자 주입을 통해 의존성을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @see RankingService
 * @see UserRepository
 * @since 2026-03-23
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final UserRepository userRepository;

    /**
     * 상위 10명의 랭킹을 조회합니다.
     * <p>
     * User 테이블의 totalRankingScore를 기준으로
     * 내림차순 정렬하여 상위 10명을 반환합니다.
     *
     * @return 상위 10명의 랭킹 정보 (순위, 닉네임, 점수)
     */
    @Override
    public RankingResponse getTopRankings() {

        List<RankingResponse.RankingItem> rankings = new ArrayList<>();
        List<User> topUsers = userRepository.findTop10ByOrderByTotalRankingScoreDesc();

        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);
            rankings.add(new RankingResponse.RankingItem(
                    i + 1,
                    user.getNickname(),
                    user.getTotalRankingScore()
            ));
        }

        return new RankingResponse(rankings);
    }
}