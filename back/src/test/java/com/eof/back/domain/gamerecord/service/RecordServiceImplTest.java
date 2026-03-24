package com.eof.back.domain.gamerecord.service;

import com.eof.back.domain.gamerecord.dto.GameResultRequest;
import com.eof.back.domain.gamerecord.entity.GameRecord;
import com.eof.back.domain.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.gamerecord.service.RecordServiceImpl;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.entity.Role;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.exceptions.AuthException;

import com.eof.back.global.exception.exceptions.GameSessionException;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RecordServiceImplTest {

    @InjectMocks
    private RecordServiceImpl recordService;

    @Mock
    private GameRecordRepository gameRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameSessionRepository gameSessionRepository;

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
        given(gameRecordRepository.findByUserIdWithSessionAndQuizSet(eq(userId), any(Pageable.class))).willReturn(page);
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
    @Test
    @DisplayName("게임 결과 저장 - 정상")
    void saveGameResult_success() {
        // given
        GameSession session = createSession(10);
        User user1 = createUser("유저1", 0L);
        User user2 = createUser("유저2", 0L);

        given(gameSessionRepository.findById(1L)).willReturn(Optional.of(session));
        given(userRepository.findAllById(anyList())).willReturn(List.of(user1, user2));

        GameResultRequest request = new GameResultRequest(1L, List.of(
                new GameResultRequest.PlayerResult(1L, 1, 950),
                new GameResultRequest.PlayerResult(2L, 2, 720)
        ));

        // when
        recordService.saveGameResult(request);

        // then
        verify(gameRecordRepository).saveAll(anyList());
        assertThat(user1.getTotalRankingScore()).isGreaterThan(0L);
        assertThat(user2.getTotalRankingScore()).isGreaterThan(0L);
        assertThat(user1.getTotalRankingScore()).isGreaterThan(user2.getTotalRankingScore());
    }

    @Test
    @DisplayName("게임 결과 저장 - 존재하지 않는 세션")
    void saveGameResult_sessionNotFound() {
        // given
        given(gameSessionRepository.findById(999L)).willReturn(Optional.empty());

        GameResultRequest request = new GameResultRequest(999L, List.of(
                new GameResultRequest.PlayerResult(1L, 1, 950)
        ));

        // when & then
        assertThatThrownBy(() -> recordService.saveGameResult(request))
                .isInstanceOf(GameSessionException.class);
    }

    @Test
    @DisplayName("게임 결과 저장 - 존재하지 않는 유저")
    void saveGameResult_userNotFound() {
        // given
        GameSession session = createSession(10);
        given(gameSessionRepository.findById(1L)).willReturn(Optional.of(session));
        given(userRepository.findAllById(anyList())).willReturn(List.of());

        GameResultRequest request = new GameResultRequest(1L, List.of(
                new GameResultRequest.PlayerResult(999L, 1, 950)
        ));

        // when & then
        assertThatThrownBy(() -> recordService.saveGameResult(request))
                .isInstanceOf(AuthException.class);
    }

    private User createUser(String nickname, Long score) {
        User user = User.builder()
                .username(nickname)
                .password("test")
                .nickname(nickname)
                .role(Role.USER)
                .build();
        try {
            var idField = User.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, nickname.equals("유저1") ? 1L : 2L);

            var scoreField = User.class.getDeclaredField("totalRankingScore");
            scoreField.setAccessible(true);
            scoreField.set(user, score);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private GameSession createSession(int maxQuizzes) {
        try {
            var constructor = GameSession.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            GameSession session = constructor.newInstance();

            var field = GameSession.class.getDeclaredField("maxQuizzes");
            field.setAccessible(true);
            field.set(session, maxQuizzes);

            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}