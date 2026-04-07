package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingProjection;
import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.infrastructure.cache.RankingCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class RankingCacheServiceTest {

    @InjectMocks
    private RankingCacheService rankingCacheService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Test
    @DisplayName("전체 랭킹 TOP10 조회 - 정상")
    void getTopRankingItems_success() {
        List<User> users = List.of(
                createUser(1L, "유저1", 5000L),
                createUser(2L, "유저2", 3000L),
                createUser(3L, "유저3", 1000L)
        );
        given(userRepository.findTop10ActiveUsers(any(Pageable.class))).willReturn(users);

        List<RankingResponse.RankingItem> result = rankingCacheService.getTopRankingItems();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).nickname()).isEqualTo("유저1");
        assertThat(result.get(0).score()).isEqualTo(5000L);
        assertThat(result.get(2).rank()).isEqualTo(3);
    }

    @Test
    @DisplayName("전체 랭킹 TOP10 조회 - 유저 없음")
    void getTopRankingItems_empty() {
        given(userRepository.findTop10ActiveUsers(any(Pageable.class))).willReturn(List.of());

        List<RankingResponse.RankingItem> result = rankingCacheService.getTopRankingItems();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("주간 랭킹 TOP10 조회 - 정상")
    void getWeeklyRankingItems_success() {
        List<RankingProjection> mockResults = List.of(
                new RankingProjection(createUser(1L, "유저1", 5000L), 5000L),
                new RankingProjection(createUser(2L, "유저2", 3000L), 3000L)
        );
        given(gameRecordRepository.findRankingByPeriod(any(), any(Pageable.class)))
                .willReturn(mockResults);

        List<RankingResponse.RankingItem> result = rankingCacheService.getWeeklyRankingItems();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).nickname()).isEqualTo("유저1");
        assertThat(result.get(0).score()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("주간 랭킹 TOP10 조회 - 데이터 없음")
    void getWeeklyRankingItems_empty() {
        given(gameRecordRepository.findRankingByPeriod(any(), any(Pageable.class)))
                .willReturn(List.of());

        List<RankingResponse.RankingItem> result = rankingCacheService.getWeeklyRankingItems();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("월간 랭킹 TOP10 조회 - 정상")
    void getMonthlyRankingItems_success() {
        List<RankingProjection> mockResults = List.of(
                new RankingProjection(createUser(1L, "유저1", 8000L), 8000L)
        );
        given(gameRecordRepository.findRankingByPeriod(any(), any(Pageable.class)))
                .willReturn(mockResults);

        List<RankingResponse.RankingItem> result = rankingCacheService.getMonthlyRankingItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).rank()).isEqualTo(1);
        assertThat(result.get(0).nickname()).isEqualTo("유저1");
        assertThat(result.get(0).score()).isEqualTo(8000L);
    }

    @Test
    @DisplayName("월간 랭킹 TOP10 조회 - 데이터 없음")
    void getMonthlyRankingItems_empty() {
        given(gameRecordRepository.findRankingByPeriod(any(), any(Pageable.class)))
                .willReturn(List.of());

        List<RankingResponse.RankingItem> result = rankingCacheService.getMonthlyRankingItems();

        assertThat(result).isEmpty();
    }

    private User createUser(Long id, String nickname, Long score) {
        User user = User.builder()
                .username(nickname)
                .password("test")
                .nickname(nickname)
                .role(Role.USER)
                .build();
        try {
            var idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);

            var scoreField = User.class.getDeclaredField("totalRankingScore");
            scoreField.setAccessible(true);
            scoreField.set(user, score);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }
}