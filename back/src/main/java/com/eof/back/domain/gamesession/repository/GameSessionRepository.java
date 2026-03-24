package com.eof.back.domain.gamesession.repository;

import com.eof.back.domain.gamesession.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * GameSession 엔티티에 대한 데이터 엑세스 기능을 제공하는 인터페이스입니다.
 * <p>
 * Spring Data JPA를 기반으로 기본적인 CRUD 기능을 수행합니다.
 *
 * @author 유재원
 * @since 2026-03-18
 */

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    /**
     * roomName에 특정 문자열이 포함된 모든 방을 찾습니다. Like쿼리사용
     */
    List<GameSession> findByRoomNameContaining(String keyword);

    /**
     * N+1 방지를 위한 Fetch Join (방 정보 + 참가자 목록 + 유저 정보 한 번에 조회)
     *
     * @param gameSessionId
     * @return
     */
    @Query("SELECT gs FROM GameSession gs " +
            "LEFT JOIN FETCH gs.host " +
            "LEFT JOIN FETCH gs.players " +
            "WHERE gs.id = :gameSessionId")
    Optional<GameSession> findByIdWithPlayers(@Param("gameSessionId") Long gameSessionId);
}
