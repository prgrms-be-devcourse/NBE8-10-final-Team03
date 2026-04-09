package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;
import com.eof.back.domain.gamesession.dto.GameSessionJoinResponse;
import com.eof.back.domain.gamesession.dto.GameSessionListResponse;
import com.eof.back.domain.gamesession.entity.GameSession;
import com.eof.back.domain.gamesession.entity.GameSessionStatus;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.exceptions.GameSessionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class GameSessionServiceTest {

    @InjectMocks
    private GameSessionServiceImpl gameSessionService;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizSetRepository quizSetRepository;

    @Mock
    private GamePlayService gamePlayService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

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
    @DisplayName("대기 중인 전체 게임 세션 조회 테스트")
    void getAllGameSessions_Success() {
        GameSession session1 = mock(GameSession.class);
        GameSession session2 = mock(GameSession.class);

        setupMockSession(session1, 1L, "방1", "테스터1", 10L, "퀴즈1");
        setupMockSession(session2, 2L, "방2", "테스터2", 20L, "퀴즈2");

        given(gameSessionRepository.findAll())
                .willReturn(List.of(session1, session2));

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

    @Test
    @DisplayName("게임 세션 삭제(상태 변경) 성공 테스트")
    void deleteGameSession_Success() {
        // given
        Long userId = 1L; // 삭제를 요청한 유저 아이디
        Long gameSessionId = 10L;

        GameSession mockSession = mock(GameSession.class);
        User mockHost = mock(User.class);

        // 방장의 ID가 요청한 유저의 ID와 동일하도록 설정
        given(mockHost.getId()).willReturn(userId);
        given(mockSession.getHost()).willReturn(mockHost);
        given(gameSessionRepository.findById(gameSessionId)).willReturn(Optional.of(mockSession));

        // when
        gameSessionService.deleteGameSession(userId, gameSessionId);

        // then
        // [수정된 부분] 레포지토리의 delete 대신 엔티티의 endGame() 호출 검증
        verify(mockSession, times(1)).endGame(); // (엔티티에 작성하신 상태 변경 메서드명으로 맞춰주세요)

        // (선택 사항) delete가 절대 호출되지 않았는지 명시적으로 검증할 수도 있습니다.
        verify(gameSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("게임 세션 삭제 실패 - 존재하지 않는 방")
    void deleteGameSession_Fail_NotFound() {
        Long userId = 1L;
        Long gameSessionId = 10L;

        // DB에서 방을 찾지 못한 상황 설정
        given(gameSessionRepository.findById(gameSessionId)).willReturn(Optional.empty());

        // 지정한 예외가 발생하는지 검증
        assertThatThrownBy(() -> gameSessionService.deleteGameSession(userId, gameSessionId))
                .isInstanceOf(GameSessionException.class);

        // delete 메서드 호출 안됨
        verify(gameSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("게임 세션 삭제 실패 - 방장이 아닌 유저가 요청")
    void deleteGameSession_Fail_Unauthorized() {
        Long userId = 1L; // 삭제를 요청한 유저 아이디 (일반 참가자)
        Long hostId = 2L; // 실제 방장의 아이디
        Long gameSessionId = 10L;

        GameSession mockSession = mock(GameSession.class);
        User mockHost = mock(User.class);

        // 방장의 ID가 요청한 유저와 다르게 설정
        given(mockHost.getId()).willReturn(hostId);
        given(mockSession.getHost()).willReturn(mockHost);
        given(gameSessionRepository.findById(gameSessionId)).willReturn(Optional.of(mockSession));

        assertThatThrownBy(() -> gameSessionService.deleteGameSession(userId, gameSessionId))
                .isInstanceOf(GameSessionException.class);

        // delete 메서드 호출 안됨
        verify(gameSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("게임 세션 입장 성공 테스트")
    void joinRoom_Success() {
        Long userId = 2L; // 입장하려는 일반 유저
        Long gameSessionId = 10L;

        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(userId);
        given(mockUser.getNickname()).willReturn("입장유저");

        User mockHost = mock(User.class);
        given(mockHost.getId()).willReturn(1L);

        QuizSet mockQuizSet = mock(QuizSet.class);
        given(mockQuizSet.getId()).willReturn(100L);

        GameSession mockSession = mock(GameSession.class);
        given(mockSession.getId()).willReturn(gameSessionId);
        given(mockSession.getRoomName()).willReturn("테스트 방");
        given(mockSession.getHost()).willReturn(mockHost);
        given(mockSession.getQuizSet()).willReturn(mockQuizSet);
        given(mockSession.getStatus()).willReturn(GameSessionStatus.WAIT);

        // join 시 불릴 로직을 위해 players 리스트 모의 반환 (자신 포함)
        given(mockSession.getPlayers()).willReturn(List.of(mockHost, mockUser));

        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(gameSessionRepository.findByIdWithPlayers(gameSessionId)).willReturn(Optional.of(mockSession));

        GameSessionJoinResponse response = gameSessionService.joinRoom(userId, gameSessionId);

        verify(mockSession, times(1)).join(mockUser); // 내부 join 로직이 호출되었는지 검증
        assertThat(response).isNotNull();
        assertThat(response.gameSessionId()).isEqualTo(gameSessionId);
        assertThat(response.players()).hasSize(2);
    }

    @Test
    @DisplayName("방 나가기 성공 - 일반 참가자가 나갈 때")
    void leaveRoom_Success_GuestLeave() {
        Long guestId = 2L;
        Long gameSessionId = 10L;

        User mockGuest = mock(User.class);
        User mockHost = mock(User.class);
        given(mockHost.getId()).willReturn(1L);

        // 1. 가짜 QuizSet 만들고 ID 설정
        com.eof.back.domain.quizset.entity.QuizSet mockQuizSet = mock(com.eof.back.domain.quizset.entity.QuizSet.class);
        given(mockQuizSet.getId()).willReturn(100L);

        // 2. Session 모킹 및 연관 관계 연결
        GameSession mockSession = mock(GameSession.class);
        given(mockSession.getHost()).willReturn(mockHost);
        given(mockSession.getQuizSet()).willReturn(mockQuizSet);
        given(mockSession.getStatus()).willReturn(com.eof.back.domain.gamesession.entity.GameSessionStatus.WAIT);

        // 4. Repository 모킹
        given(userRepository.findById(guestId)).willReturn(Optional.of(mockGuest));
        given(gameSessionRepository.findByIdWithPlayers(gameSessionId)).willReturn(Optional.of(mockSession));

        // 5. 실행 및 검증
        gameSessionService.leaveRoom(guestId, gameSessionId);

        verify(mockSession, times(1)).leave(mockGuest);
        verify(gameSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("방 나가기 성공 -    방장이 나갈 때 (방 폭파)")
    void leaveRoom_Success_HostLeave() {
        Long hostId = 1L;
        Long gameSessionId = 10L;

        User mockHost = mock(User.class);
        given(mockHost.getId()).willReturn(hostId);

        GameSession mockSession = mock(GameSession.class);
        given(mockSession.getHost()).willReturn(mockHost); // 요청자와 방장이 일치함

        given(userRepository.findById(hostId)).willReturn(Optional.of(mockHost));
        given(gameSessionRepository.findByIdWithPlayers(gameSessionId)).willReturn(Optional.of(mockSession));

        gameSessionService.leaveRoom(hostId, gameSessionId);

        verify(mockSession, times(1)).endGame(); //

        verify(mockSession, never()).leave(any()); // 일반 퇴장 로직은 실행되지 않아야 함
    }

    @Test
    @DisplayName("방 설정 수정 성공 테스트")
    void updateGameSession_Success() {
        Long userId = 1L;
        Long gameSessionId = 100L;
        Long newQuizSetId = 20L;
        com.eof.back.domain.gamesession.dto.GameSessionUpdateRequest updateRequest = new com.eof.back.domain.gamesession.dto.GameSessionUpdateRequest(
                "새로운 방 이름", newQuizSetId, 5, 15
        );

        User mockHost = mock(User.class);
        given(mockHost.getId()).willReturn(userId);

        QuizSet mockNewQuizSet = mock(QuizSet.class);

        GameSession mockSession = mock(GameSession.class);
        given(mockSession.getHost()).willReturn(mockHost);
        given(mockSession.getQuizSet()).willReturn(mockNewQuizSet);
        given(mockSession.getStatus()).willReturn(GameSessionStatus.WAIT);

        given(gameSessionRepository.findByIdWithPlayers(gameSessionId)).willReturn(Optional.of(mockSession));
        given(quizSetRepository.findById(newQuizSetId)).willReturn(Optional.of(mockNewQuizSet));

        gameSessionService.updateGameSession(userId, gameSessionId, updateRequest);

        verify(mockSession, times(1)).updateRoom("새로운 방 이름", mockNewQuizSet, 15, 5);
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    @DisplayName("게임 세션 생성 실패 - 유저 없음")
    void createGameSession_Fail_UserNotFound() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        assertThatThrownBy(() -> gameSessionService.createGameSession(1L, mock(GameSessionCreateRequest.class)))
                .isInstanceOf(com.eof.back.global.exception.exceptions.AuthException.class);
    }

    @Test
    @DisplayName("게임 세션 생성 실패 - 퀴즈 세트 없음")
    void createGameSession_Fail_QuizSetNotFound() {
        GameSessionCreateRequest request = new GameSessionCreateRequest("방", 1L, 4, 10);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(mock(User.class)));
        given(quizSetRepository.findById(anyLong())).willReturn(Optional.empty());
        
        assertThatThrownBy(() -> gameSessionService.createGameSession(1L, request))
                .isInstanceOf(com.eof.back.global.exception.exceptions.QuizSetException.class);
    }

    @Test
    @DisplayName("방 입장 실패 - 대기 상태가 아님")
    void joinRoom_Fail_InvalidStatus() {
        GameSession mockSession = mock(GameSession.class);
        User mockUser = mock(User.class);
        given(mockSession.getStatus()).willReturn(GameSessionStatus.START);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(mockUser));
        given(gameSessionRepository.findByIdWithPlayers(anyLong())).willReturn(Optional.of(mockSession));

        assertThatThrownBy(() -> gameSessionService.joinRoom(1L, 1L))
                .isInstanceOf(GameSessionException.class);
    }

    @Test
    @DisplayName("방 설정 수정 실패 - 방장이 아님")
    void updateGameSession_Fail_NotHost() {
        User mockHost = mock(User.class);
        given(mockHost.getId()).willReturn(2L);
        GameSession mockSession = mock(GameSession.class);
        given(mockSession.getHost()).willReturn(mockHost);
        given(gameSessionRepository.findByIdWithPlayers(anyLong())).willReturn(Optional.of(mockSession));

        assertThatThrownBy(() -> gameSessionService.updateGameSession(1L, 1L, mock(com.eof.back.domain.gamesession.dto.GameSessionUpdateRequest.class)))
                .isInstanceOf(GameSessionException.class);
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