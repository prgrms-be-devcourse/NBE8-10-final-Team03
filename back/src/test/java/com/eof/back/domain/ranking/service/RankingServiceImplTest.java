package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @InjectMocks
    private RankingServiceImpl rankingService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("상위 랭킹 조회 - 정상")
    void getTopRankings_success() {
        List<User> users = List.of(
                createUser("유저1", 5000L),
                createUser("유저2", 3000L),
                createUser("유저3", 1000L)
        );
        given(userRepository.findTop10ByOrderByTotalRankingScoreDesc()).willReturn(users);

        RankingResponse response = rankingService.getTopRankings();

        assertThat(response.rankings()).hasSize(3);
        assertThat(response.rankings().get(0).rank()).isEqualTo(1);
        assertThat(response.rankings().get(0).nickname()).isEqualTo("유저1");
        assertThat(response.rankings().get(0).totalRankingScore()).isEqualTo(5000L);
        assertThat(response.rankings().get(2).rank()).isEqualTo(3);
    }

    @Test
    @DisplayName("상위 랭킹 조회 - 유저 없음")
    void getTopRankings_empty() {
        given(userRepository.findTop10ByOrderByTotalRankingScoreDesc()).willReturn(List.of());

        RankingResponse response = rankingService.getTopRankings();

        assertThat(response.rankings()).isEmpty();
    }

    private User createUser(String nickname, Long score) {
        User user = User.builder()
                .username(nickname)
                .password("test")
                .nickname(nickname)
                .role(Role.USER)
                .build();
        try {
            var field = User.class.getDeclaredField("totalRankingScore");
            field.setAccessible(true);
            field.set(user, score);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }
}