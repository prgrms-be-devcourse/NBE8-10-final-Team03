package com.eof.back.domain.gamerecord.service;

import com.eof.back.domain.gamerecord.entity.GameRecord;
import com.eof.back.domain.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.gamerecord.service.RecordServiceImpl;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.exceptions.AuthException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code RecordServiceImplTest(String example)} <br>
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
 * @since 2026-03-19
 */
@ExtendWith(MockitoExtension.class)
class RecordServiceImplTest {

    QuizSet quizSet = QuizSet.builder()
            .title("테스트 퀴즈셋")
            .build();

    GameSession session = GameSession.builder()
            .quizSet(quizSet)
            .maxPlayers(4)
            .build();

    @InjectMocks
    private RecordServiceImpl recordService;

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("내 전적 조회 - 정상")
    void getMyRecords_success() {
        // given
        Long userId = 1L;
        User user = User.builder().build();

        QuizSet quizSet = mock(QuizSet.class);
        given(quizSet.getTitle()).willReturn("테스트 퀴즈셋");

        GameSession session = mock(GameSession.class);
        given(session.getQuizSet()).willReturn(quizSet);
        given(session.getMaxPlayers()).willReturn(4);

        GameRecord record = GameRecord.builder()
                .user(user)
                .gameSession(session)
                .sessionScore(850)
                .sessionRanking(1)
                .earnedRankingScore(210L)
                .build();

        Page<GameRecord> page = new PageImpl<>(
                List.of(record), PageRequest.of(0, 10), 1
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(gameRecordRepository.findByUserId(eq(userId), any(Pageable.class))).willReturn(page);
        given(gameRecordRepository.countByUserId(userId)).willReturn(1L);
        given(gameRecordRepository.countByUserIdAndSessionRanking(userId, 1)).willReturn(1L);

        // when
        UserRecordResponse response = recordService.getMyRecords(userId, 0, 10);

        // then
        assertThat(response.totalGames()).isEqualTo(1);
        assertThat(response.totalWins()).isEqualTo(1);
        assertThat(response.recentRecords()).hasSize(1);
    }

    @Test
    @DisplayName("내 전적 조회 - 존재하지 않는 유저")
    void getMyRecords_userNotFound() {

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getMyRecords(999L, 0, 10))
                .isInstanceOf(AuthException.class);
    }
}