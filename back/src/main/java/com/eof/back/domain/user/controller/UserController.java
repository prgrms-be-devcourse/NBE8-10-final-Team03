package com.eof.back.domain.user.controller;

import com.eof.back.domain.user.dto.*;
import com.eof.back.domain.user.service.UserService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 관련 API 요청을 처리하는 컨트롤러입니다.
 *
 * <p>회원가입, 로그인, 사용자 정보 조회 등
 * 사용자 도메인과 관련된 HTTP 요청을 처리합니다.</p>
 *
 * <p><b>주요 기능:</b><br>
 * - 회원가입
 * - 로그인
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@RestController
@RequestMapping(UserController.USERS_URI)
@RequiredArgsConstructor
public class UserController {
    public static final String USERS_URI = "/api/v1/auth";
    private final UserService userService;

    /**
     * 회원가입을 처리합니다.
     *
     * @param request 회원가입 요청 데이터
     * @return 생성된 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<Response<UserSignupResponse>> signup(
            @Valid @RequestBody UserSignupRequest request
    ) {
        UserSignupResponse response = userService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(response, "회원가입이 완료되었습니다."));
    }

    /**
     * 로그인을 처리합니다.
     *
     * <p>인증이 성공하면 JWT AccessToken 및 RefreshToken을 발급하여 반환합니다.
     *
     * @param request 로그인 요청 데이터 (username, password)
     * @return AccessToken, RefreshToken 및 사용자 기본 정보 (200 OK)
     */
    @PostMapping("/login")
    public ResponseEntity<Response<UserLoginResponse>> login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        UserLoginResponse response = userService.login(request);

        return ResponseEntity.ok(
                CommonResponse.success(response, "로그인에 성공했습니다.")
        );
    }

    /**
     * 로그인한 사용자의 정보를 조회합니다.
     *
     * @param userId 로그인한 사용자 ID
     * @return 사용자 기본 정보
     */
    @GetMapping("/me")
    public ResponseEntity<Response<UserInfoResponse>> getMyInfo(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        UserInfoResponse response = userService.getMyInfo(userId);

        return ResponseEntity.ok(
                CommonResponse.success(response, "내 정보 조회에 성공했습니다.")
        );
    }
}
