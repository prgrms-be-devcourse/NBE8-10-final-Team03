package com.eof.back.domain.ranking.service;

import com.eof.back.domain.ranking.dto.RankingResponseDto;
import com.eof.back.domain.user.entity.Role;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import static org.mockito.BDDMockito.given;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code RankingServiceImplTest(String example)} <br>
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

        RankingResponseDto response = rankingService.getTopRankings();

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

        RankingResponseDto response = rankingService.getTopRankings();

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