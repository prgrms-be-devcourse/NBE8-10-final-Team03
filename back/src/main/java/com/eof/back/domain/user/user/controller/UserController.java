package com.eof.back.domain.user.user.controller;

import com.eof.back.domain.user.user.dto.UserInfoResponse;
import com.eof.back.domain.user.user.dto.UserUpdateRequest;
import com.eof.back.domain.user.user.dto.UserUpdateResponse;
import com.eof.back.domain.user.user.service.UserService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API 요청을 처리하는 컨트롤러입니다.
 *
 * <p>사용자 정보 조회 등 사용자 도메인과 관련된 HTTP 요청을 처리합니다.</p>
 *
 * <p><b>주요 기능:</b><br>
 * - 내 정보 조회
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@RestController
@RequestMapping("/api/v1/users/{userId}")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자의 정보를 조회합니다.
     *
     * @param userId 조회할 사용자의 식별자
     * @return 사용자 기본 정보
     */
    @PreAuthorize("authentication.principal.id == #userId")
    @GetMapping
    public ResponseEntity<Response<UserInfoResponse>> getMyInfo(
            @PathVariable Long userId
    ) {
        UserInfoResponse response = userService.getInfo(userId);

        return ResponseEntity.ok(
                CommonResponse.success(response, "내 정보 조회에 성공했습니다.")
        );
    }

    /**
     * 사용자의 정보를 수정합니다.
     *
     * @param userId  수정할 사용자의 식별자
     * @param request 수정할 정보 (닉네임, 비밀번호)
     * @return 수정된 사용자 기본 정보
     */
    @PreAuthorize("authentication.principal.id == #userId")
    @PatchMapping
    public ResponseEntity<Response<UserUpdateResponse>> updateMyInfo(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateInfo(userId, request);

        return ResponseEntity.ok(
                CommonResponse.success(response, "내 정보 수정이 완료되었습니다.")
        );
    }
}
