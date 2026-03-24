package com.eof.back.domain.user.controller;

import com.eof.back.domain.user.dto.UserInfoResponse;
import com.eof.back.domain.user.dto.UserPrincipal;
import com.eof.back.domain.user.dto.UserUpdateRequest;
import com.eof.back.domain.user.dto.UserUpdateResponse;
import com.eof.back.domain.user.service.UserService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 로그인한 사용자의 정보를 조회합니다.
     *
     * @param principal 로그인한 사용자 인증 정보
     * @return 사용자 기본 정보
     */
    @GetMapping("/me")
    public ResponseEntity<Response<UserInfoResponse>> getMyInfo(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserInfoResponse response = userService.getInfo(principal.id());

        return ResponseEntity.ok(
                CommonResponse.success(response, "내 정보 조회에 성공했습니다.")
        );
    }

    /**
     * 로그인한 사용자의 정보를 수정합니다.
     *
     * @param userPrincipal 로그인한 사용자 인증 정보
     * @param request       수정할 정보 (닉네임, 비밀번호)
     * @return 수정된 사용자 기본 정보
     */
    @PatchMapping("/me")
    public ResponseEntity<Response<UserUpdateResponse>> updateMyInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateInfo(userPrincipal.id(), request);

        return ResponseEntity.ok(
                CommonResponse.success(response, "내 정보 수정이 완료되었습니다.")
        );
    }
}
