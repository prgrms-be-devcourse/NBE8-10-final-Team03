package com.eof.back.domain.gamerecord.controller;

import com.eof.back.domain.gamerecord.dto.UserRecordResponse;
import com.eof.back.domain.gamerecord.service.RecordService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code RecordController(String example)} <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author Jaewon Ryu
 * @see
 * @since 2026-03-20
 */
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    @GetMapping("/records")
    public ResponseEntity<Response<UserRecordResponse>> getMyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = 1L; // TODO: 인증된 사용자 ID로 변경
        return ResponseEntity.ok(
                CommonResponse.success(
                        recordService.getMyRecords(userId, page, size),
                        "전적 조회 성공"
                )
        );
    }
}