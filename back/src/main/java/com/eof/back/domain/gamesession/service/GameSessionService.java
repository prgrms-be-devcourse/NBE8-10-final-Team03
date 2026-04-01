package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;
import com.eof.back.domain.gamesession.dto.GameSessionJoinResponse;
import com.eof.back.domain.gamesession.dto.GameSessionListResponse;

import java.util.List;

/**
 * 게임세션(room)관련 비즈니스 로직을 정의하는 인터페이스입니다.
 *
 * @author 유재원
 * @since 2026-03-18
 */
public interface GameSessionService {
    /**
     * 새로운 게임세션을 생성합니다.
     * 게임방의 방장은 생성자가 됩니다.
     *
     * @param userId  게임세션의 host가 됩니다.
     * @param request 게임세션의 생성 요청 정보
     * @return 게임세션의 정보 DTO를 리턴합니다.
     */
    GameSessionCreateResponse createGameSession(Long userId, GameSessionCreateRequest request);

    /**
     * 입력된 방 이름(키워드)이 포함된 게임 세션 목록을 검색하여 반환합니다.
     * Like 검색을 통해 일부 단어만 일치해도 결과를 가져옵니다.
     *
     * @param roomName 검색할 방 이름의 키워드
     * @return 검색 조건에 일치하는 게임 세션 정보 DTO 목록을 리턴합니다.
     */
    List<GameSessionListResponse> getGameSessionByRoomName(String roomName);

    /**
     * 현재 데이터베이스에 존재하는 모든 게임 세션 목록을 조회하여 반환합니다.
     *
     * @return 전체 게임 세션 정보 DTO 목록을 리턴합니다.
     */
    List<GameSessionListResponse> getAllGameSessions();

    /**
     * 유저가 호스트인지 검증하고 게임세션을 삭제합니다.
     *
     * @param userId        삭제요청을 보낸 유저의 아이디
     * @param gameSessionId 게임세션의 아이디
     */
    void deleteGameSession(Long userId, Long gameSessionId);

    /**
     * 유저가 게임세션 room에 들어감
     *
     * @param userId        해당 유저의 아이디
     * @param gameSessionId 게임 세션의 아이디
     * @return 입장한 게임세션의 정보 DTO를 리턴
     */

    GameSessionJoinResponse joinRoom(Long userId, Long gameSessionId);

    /**
     * 유저가 게임세션 room에서 나감
     *
     * @param userId        해당 유저의 아이디
     * @param gameSessionId 게임 세션의 아이디
     */
    void leaveRoom(Long userId, Long gameSessionId);
}
