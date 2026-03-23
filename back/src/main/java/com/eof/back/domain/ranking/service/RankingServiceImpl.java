package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponseDto;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code RankingServiceImpl(String example)} <br>
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
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final UserRepository userRepository;

    @Override
    public RankingResponseDto getTopRankings() {

        List<RankingResponseDto.RankingItem> rankings = new ArrayList<>();
        List<User> topUsers = userRepository.findTop10ByOrderByTotalRankingScoreDesc();

        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);
            rankings.add(new RankingResponseDto.RankingItem(
                    i + 1,
                    user.getNickname(),
                    user.getTotalRankingScore()
            ));
        }

        return new RankingResponseDto(rankings);
    }
}