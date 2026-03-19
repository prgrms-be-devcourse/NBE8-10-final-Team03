package com.eof.back.domain.gamerecord.service;

import com.eof.back.domain.gamerecord.dto.UserRecordResponse;

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
    UserRecordResponse getMyRecords(Long userId, int page, int size);
}
