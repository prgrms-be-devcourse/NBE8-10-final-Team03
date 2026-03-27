package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponse;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
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

    @Test
    @DisplayName("상위 랭킹 조회 - 정상")
    void getTopRankings_success() {
        List<User> users = List.of(
                createUser(1L, "유저1", 5000L),
                createUser(2L, "유저2", 3000L),
                createUser(3L, "유저3", 1000L)
        );
        given(userRepository.findTop10ActiveUsers(any(Pageable.class))).willReturn(users);
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
        given(userRepository.findTop10ActiveUsers(any(Pageable.class))).willReturn(List.of());
        given(userRepository.findMyRankByUserId(any(Long.class))).willReturn(1L);

        RankingResponse response = rankingService.getTopRankings(1L);

        assertThat(response.rankings()).isEmpty();
    }

    @Test
    @DisplayName("상위 랭킹 조회 - 비로그인")
    void getTopRankings_notLoggedIn() {
        given(userRepository.findTop10ActiveUsers(any(Pageable.class))).willReturn(List.of());

        RankingResponse response = rankingService.getTopRankings(null);

        assertThat(response.myRank()).isNull();
        assertThat(response.rankings()).isEmpty();
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