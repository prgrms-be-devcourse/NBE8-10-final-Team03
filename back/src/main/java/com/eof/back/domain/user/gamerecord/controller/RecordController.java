package com.eof.back.domain.user.gamerecord.controller;

import com.eof.back.domain.user.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.user.gamerecord.service.RecordService;
import com.eof.back.domain.user.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.user.gamerecord.service.RecordService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 인증된 사용자의 게임 전적을 조회하는 REST 컨트롤러입니다.
 * <p>
 * {@code /api/v1/users/{userId}/records} 경로로 들어오는 요청을 처리하며,
 * {@link RecordService}를 통해 전적 데이터를 조회하여 반환합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @RestController}로 등록되며, 생성자 주입을 통해 의존성을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @see RecordService
 * @see UserRecordResponse
 * @since 2026-03-20
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/records")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    /**
     * 사용자의 게임 전적을 페이지 단위로 조회합니다.
     *
     * @param userId 조회할 사용자의 식별자
     * @param page   조회할 페이지 번호 (기본값: 0)
     * @param size   페이지당 조회 개수 (기본값: 10)
     * @return 전적 조회 결과 (총 게임 수, 우승 수, 랭킹 점수, 최근 전적 목록)
     */
    @PreAuthorize("authentication.principal.id == #userId")
    @GetMapping
    public ResponseEntity<Response<UserRecordResponse>> getMyRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        return ResponseEntity.ok(
                CommonResponse.success(
                        recordService.getMyRecords(userId, page, size),
                        "전적 조회 성공"
                )
        );
    }
}