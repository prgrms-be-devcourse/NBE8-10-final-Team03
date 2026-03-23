package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;
import com.eof.back.domain.gamesession.dto.GameSessionListResponse;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.entity.GameSessionStatus;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class GameSessionServiceTest {

    @InjectMocks
    private GameSessionImpl gameSessionService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizSetRepository quizSetRepository;

    @Test
    @DisplayName("게임 세션 생성 성공 테스트")
    void createGameSession_Success() {
        Long userId = 1L;
        Long quizSetId = 10L;
        GameSessionCreateRequest request = new GameSessionCreateRequest(
                "테스트 방", quizSetId, 4, 10
        );

        User mockUser = mock(User.class);
        QuizSet mockQuizSet = mock(QuizSet.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(quizSetRepository.findById(quizSetId)).willReturn(Optional.of(mockQuizSet));

        GameSession mockSavedSession = mock(GameSession.class);
        given(mockSavedSession.getId()).willReturn(100L);
        given(mockSavedSession.getRoomName()).willReturn(request.roomName());
        given(mockSavedSession.getHost()).willReturn(mockUser);
        given(mockSavedSession.getQuizSet()).willReturn(mockQuizSet);
        given(mockSavedSession.getMaxPlayers()).willReturn(request.maxPlayers());
        given(mockSavedSession.getStatus()).willReturn(GameSessionStatus.WAIT);
        given(mockSavedSession.getMaxQuizzes()).willReturn(request.maxQuizzes());

        given(gameSessionRepository.save(any(GameSession.class))).willReturn(mockSavedSession);

        GameSessionCreateResponse response = gameSessionService.createGameSession(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.gameSessionId()).isEqualTo(100L);
        assertThat(response.roomName()).isEqualTo("테스트 방");
        assertThat(response.maxPlayers()).isEqualTo(4);
    }

    @Test
    @DisplayName("전체 게임 세션 조회 테스트")
    void getAllGameSessions_Success() {
        GameSession session1 = mock(GameSession.class);
        GameSession session2 = mock(GameSession.class);

        setupMockSession(session1, 1L, "방1", "테스터1", 10L, "퀴즈1");
        setupMockSession(session2, 2L, "방2", "테스터2", 20L, "퀴즈2");

        given(gameSessionRepository.findAll()).willReturn(List.of(session1, session2));

        List<GameSessionListResponse> responses = gameSessionService.getAllGameSessions();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).roomName()).isEqualTo("방1");
    }

    @Test
    @DisplayName("방 이름으로 게임 세션 검색 테스트")
    void getGameSessionByRoomName_Success() {
        String keyword = "상식";
        GameSession session = mock(GameSession.class);
        setupMockSession(session, 1L, "상식 배틀방", "테스터", 10L, "퀴즈");

        given(gameSessionRepository.findByRoomNameContaining(keyword)).willReturn(List.of(session));

        List<GameSessionListResponse> responses = gameSessionService.getGameSessionByRoomName(keyword);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).roomName()).isEqualTo("상식 배틀방");
    }

    private void setupMockSession(GameSession session, Long id, String roomName, String hostName, Long quizId, String quizTitle) {
        User host = mock(User.class);
        QuizSet quizSet = mock(QuizSet.class);

        given(host.getNickname()).willReturn(hostName);
        given(quizSet.getId()).willReturn(quizId);
        given(quizSet.getTitle()).willReturn(quizTitle);

        given(session.getId()).willReturn(id);
        given(session.getRoomName()).willReturn(roomName);
        given(session.getHost()).willReturn(host);
        given(session.getQuizSet()).willReturn(quizSet);
        given(session.getCurrentPlayersCount()).willReturn(1);
        given(session.getMaxPlayers()).willReturn(4);
        given(session.getStatus()).willReturn(GameSessionStatus.WAIT);
    }
}