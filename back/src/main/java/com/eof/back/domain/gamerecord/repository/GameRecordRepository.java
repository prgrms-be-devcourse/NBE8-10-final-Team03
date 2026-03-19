package com.eof.back.domain.gamerecord.repository;

import com.eof.back.domain.gamerecord.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * GameRecord 엔티티에 대한 데이터 접근을 담당하는 Repository입니다.
 * <p>
 * 게임 종료 후 저장된 개인별 성과 기록을 조회하는 기능을 제공합니다.
 * 전적 조회, 총 게임 수, 승리 횟수 등의 통계 데이터를 추출할 때 사용됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link JpaRepository}를 상속받아 기본 CRUD 기능을 제공합니다.
 *
 * @author Jaewon Ryu
 * @see GameRecord
 * @since 2026-03-18
 */

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    /**
     * 특정 유저의 게임 기록을 최신순으로 조회합니다.
     *
     * @param userId 조회할 유저의 ID
     * @return 해당 유저의 게임 기록 목록 (최신순 정렬)
     */
    List<GameRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}