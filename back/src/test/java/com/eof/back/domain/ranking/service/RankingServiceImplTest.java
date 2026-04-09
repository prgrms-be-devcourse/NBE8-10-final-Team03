package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.cache.RankingCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class RankingServiceImplTest {

    @InjectMocks
    private RankingServiceImpl rankingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Mock
    private RankingCacheService rankingCacheService; // 추가

    @Test
    @DisplayName("상위 랭킹 조회 - 정상")
    void getTopRankings_success() {
        List<RankingResponse.RankingItem> items = List.of(
                new RankingResponse.RankingItem(1, "유저1", 5000L,1),
                new RankingResponse.RankingItem(2, "유저2", 3000L,1),
                new RankingResponse.RankingItem(3, "유저3", 1000L,1)
        );
        given(rankingCacheService.getTopRankingItems()).willReturn(items);
        given(userRepository.findMyRankByUserId(any(Long.class))).willReturn(1L);

        RankingResponse response = rankingService.getTopRankings(1L);

        assertThat(response.myRank()).isEqualTo(1L);
        assertThat(response.rankings()).hasSize(3);
        assertThat(response.rankings().get(0).rank()).isEqualTo(1);
        assertThat(response.rankings().get(0).nickname()).isEqualTo("유저1");
        assertThat(response.rankings().get(0).score()).isEqualTo(5000L);
        assertThat(response.rankings().get(2).rank()).isEqualTo(3);
    }

    @Test
    @DisplayName("상위 랭킹 조회 - 유저 없음")
    void getTopRankings_empty() {
        given(rankingCacheService.getTopRankingItems()).willReturn(List.of());
        given(userRepository.findMyRankByUserId(any(Long.class))).willReturn(1L);

        RankingResponse response = rankingService.getTopRankings(1L);

        assertThat(response.rankings()).isEmpty();
    }

    @Test
    @DisplayName("상위 랭킹 조회 - 비로그인")
    void getTopRankings_notLoggedIn() {
        given(rankingCacheService.getTopRankingItems()).willReturn(List.of());

        RankingResponse response = rankingService.getTopRankings(null);

        assertThat(response.myRank()).isNull();
        assertThat(response.rankings()).isEmpty();
    }

    @Test
    @DisplayName("주간 랭킹 조회 - 성공")
    void getWeeklyRankings_Success() {
        // given
        List<RankingResponse.RankingItem> items = List.of(
                new RankingResponse.RankingItem(1, "주간1", 100L, 1)
        );
        given(rankingCacheService.getWeeklyRankingItems()).willReturn(items);

        // when
        RankingResponse response = rankingService.getWeeklyRankings(1L);

        // then
        assertThat(response.rankings()).hasSize(1);
        assertThat(response.rankings().get(0).nickname()).isEqualTo("주간1");
    }

    @Test
    @DisplayName("월간 랭킹 조회 - 성공")
    void getMonthlyRankings_Success() {
        // given
        List<RankingResponse.RankingItem> items = List.of(
                new RankingResponse.RankingItem(1, "월간1", 200L, 1)
        );
        given(rankingCacheService.getMonthlyRankingItems()).willReturn(items);

        // when
        RankingResponse response = rankingService.getMonthlyRankings(1L);

        // then
        assertThat(response.rankings()).hasSize(1);
        assertThat(response.rankings().get(0).nickname()).isEqualTo("월간1");
    }
    }