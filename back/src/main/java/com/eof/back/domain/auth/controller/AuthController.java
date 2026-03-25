package com.eof.back.domain.auth.controller;

import com.eof.back.domain.auth.dto.LoginRequest;
import com.eof.back.domain.auth.dto.LoginResponse;
import com.eof.back.domain.auth.dto.LogoutRequest;
import com.eof.back.domain.auth.dto.ReissueRequest;
import com.eof.back.domain.auth.dto.ReissueResponse;
import com.eof.back.domain.auth.dto.SignupRequest;
import com.eof.back.domain.auth.dto.SignupResponse;
import com.eof.back.domain.auth.service.AuthService;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.eof.back.global.jwt.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


/**
 * 인증 관련 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 로그인, Refresh Token을 이용한 토큰 재발급과 로그아웃 요청을 처리합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 별도의 상속 없이 인증 API 엔드포인트를 제공하는 컨트롤러입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code AuthController(AuthService authService)} <br>
 * 인증 비즈니스 로직 처리를 위해 AuthService를 주입받습니다.
 *
 * <p><b>빈 관리:</b><br>
 * Spring MVC의 REST 컨트롤러 Bean으로 등록되어 요청을 처리합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Web MVC와 Jakarta Validation을 사용합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 사용자 정보를 검증하고 계정을 생성합니다.
     *
     * @param request 회원가입 요청 DTO (username, password, nickname)
     * @return 생성된 사용자 기본 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<Response<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        SignupResponse response = authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(response, "회원가입이 완료되었습니다."));
    }

    /**
     * 사용자 자격증명을 검증하고 AccessToken과 RefreshToken을 발급합니다.
     *
     * @param request 로그인 요청 DTO (username, password)
     * @return 발급된 AccessToken, RefreshToken 및 사용자 기본 정보
     */
    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                CommonResponse.success(response, "로그인에 성공했습니다.")
        );
    }

    /**
     * Refresh Token을 이용해 Access Token과 Refresh Token을 재발급합니다.
     *
     * @param request 재발급 요청 DTO
     * @return 새로 발급된 Access Token, Refresh Token
     */
    @PostMapping("/reissue")
    public ResponseEntity<Response<ReissueResponse>> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        ReissueResponse response = authService.reissue(request.refreshToken());

        return ResponseEntity.ok(
                CommonResponse.success(response, "토큰이 재발급되었습니다.")
        );
    }

    /**
     * Refresh Token을 검증한 뒤 저장소에서 삭제하여 로그아웃 처리합니다.
     *
     * @param request 로그아웃 요청 DTO
     * @return 로그아웃 성공 응답
     */
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request.refreshToken());

        return ResponseEntity.ok(
                CommonResponse.success(null, "로그아웃이 완료되었습니다.")
        );
    }

    /**
     * 인증된 사용자를 탈퇴 처리합니다. (soft delete)
     *
     * @param principal 현재 로그인한 사용자의 인증 정보
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        authService.withdraw(principal.id());

        return ResponseEntity.noContent().build();
    }
}
