package com.eof.back.domain.gamesession.service;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;

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
}
