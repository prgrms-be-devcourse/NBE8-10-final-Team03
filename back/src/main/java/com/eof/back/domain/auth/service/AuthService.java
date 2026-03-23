package com.eof.back.domain.auth.service;

import com.eof.back.domain.auth.dto.LoginRequest;
import com.eof.back.domain.auth.dto.LoginResponse;
import com.eof.back.domain.auth.dto.ReissueResponse;
import com.eof.back.domain.auth.dto.SignupRequest;
import com.eof.back.domain.auth.dto.SignupResponse;

/**
 * 인증 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 로그인, 로그아웃, 토큰 재발급 등 인증/인가 흐름에 필요한 기능을 제공합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 별도의 상속 없이 인증 서비스의 계약을 정의하는 인터페이스입니다.
 *
 * <p><b>주요 메서드:</b><br>
 * {@code signup(SignupRequest)} <br>
 * 사용자 정보를 검증하고 계정을 생성합니다. <br>
 *
 * {@code login(LoginRequest)} <br>
 * 사용자 자격증명을 검증하고 AccessToken과 RefreshToken을 발급합니다. <br>
 *
 * {@code reissue(String refreshToken)} <br>
 * Refresh Token을 검증한 뒤 새로운 Access Token과 Refresh Token을 재발급합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public interface AuthService {

    SignupResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);

    ReissueResponse reissue(String refreshToken);

    void logout(String refreshToken);
}
