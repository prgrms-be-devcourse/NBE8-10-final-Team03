package com.eof.back.domain.gamerecord.service;

import com.eof.back.domain.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.gamerecord.entity.GameRecord;
import com.eof.back.domain.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유저의 게임 전적 조회 비즈니스 로직 구현체입니다.
 * <p>
 * {@link GameRecordRepository}를 통해 전적 데이터를 조회하고,
 * {@link UserRepository}를 통해 유저의 누적 랭킹 포인트를 가져옵니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link RecordService}의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service}로 등록되며, 생성자 주입을 통해 의존성을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @see RecordService
 * @see GameRecordRepository
 * @see UserRepository
 * @since 2026-03-19
 */
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {
    private final GameRecordRepository gameRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserRecordResponse getMyRecords(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(
                        AuthErrorCode.USER_NOT_FOUND,
                        "[RecordServiceImpl#getMyRecords] userId: " + userId
                ));

        long totalRankingScore = user.getTotalRankingScore();

        long totalGames = gameRecordRepository.countByUserId(userId);
        long totalWins = gameRecordRepository.countByUserIdAndSessionRanking(userId, 1);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<GameRecord> result = gameRecordRepository.findByUserId(userId, pageable);

        return new UserRecordResponse(
                totalGames,
                totalWins,
                totalRankingScore,
                result.getContent().stream().map(UserRecordResponse.RecordItem::from).toList(),
                page,
                size,
                result.getTotalElements()
        );
    }
}
