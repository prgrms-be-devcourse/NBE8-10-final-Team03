package com.eof.back.domain.user.gamerecord.service;

import com.eof.back.domain.user.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.user.gamerecord.dto.GameResultRequest;

/**
 * 유저의 게임 전적 조회 비즈니스 로직을 정의하는 인터페이스입니다.
 * <p>
 * 유저의 최근 게임 기록, 총 게임 수, 승리 횟수 등의 전적 데이터를
 * 페이징 처리하여 제공합니다.
 *
 * @author Jaewon Ryu
 * @see RecordServiceImpl
 * @see UserRecordResponse
 * @since 2026-03-19
 */
public interface RecordService {

    /**
     * 인증된 사용자의 게임 전적을 페이지 단위로 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @param page 조회할 페이지 번호
     * @param size 페이지당 조회 개수
     * @return 전적 조회 결과 (총 게임 수, 우승 수, 랭킹 점수, 최근 전적 목록)
     */
    UserRecordResponse getMyRecords(Long userId, int page, int size);

    /**
     * 게임 종료 시 참여자들의 전적을 저장합니다.
     * <p>
     * 유저별 GameRecord를 생성하고,
     * User의 totalRankingScore에 획득 점수를 가산합니다.
     *
     * @param request 게임 결과 정보 (세션 ID, 유저별 순위/점수)
     */
    void saveGameResult(GameResultRequest request);
}
