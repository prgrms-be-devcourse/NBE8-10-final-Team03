package com.eof.back.domain.user.controller;

import com.eof.back.domain.user.dto.UserLoginRequest;
import com.eof.back.domain.user.dto.UserLoginResponse;
import com.eof.back.domain.user.dto.UserSignupRequest;
import com.eof.back.domain.user.dto.UserSignupResponse;
import com.eof.back.domain.user.service.UserService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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

        return ResponseEntity.created(URI.create(USERS_URI + "/" + response.userId()))
                .body(CommonResponse.success(response, "회원가입이 완료되었습니다."));
    }

    /**
     * 로그인을 처리합니다.
     *
     * <p>인증이 성공하면 JWT AccessToken 및 RefreshToken을 발급하여 반환합니다.
     *
     * @param request 로그인 요청 데이터 (username, password)
     * @return 생성된 사용자 정보 (201 Created)
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
}
