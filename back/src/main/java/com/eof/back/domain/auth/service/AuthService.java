package com.eof.back.domain.auth.service;

import com.eof.back.domain.auth.dto.LoginRequest;
import com.eof.back.domain.auth.dto.LoginResult;
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
 * <p><b>토큰 전달 방식:</b><br>
 * AccessToken, RefreshToken은 HttpOnly 쿠키로 전달됩니다.
 * 서비스 메서드는 {@link LoginResult}를 반환하고,
 * 컨트롤러가 이 결과에서 토큰을 꺼내 쿠키에 설정합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public interface AuthService {

    /**
     * 사용자 정보를 검증하고 계정을 생성합니다.
     *
     * @param request 회원가입 요청 DTO (username, password, nickname)
     * @return 생성된 사용자 기본 정보
     * @throws com.eof.back.global.exception.exceptions.AuthException 아이디/닉네임 중복 시
     */
    SignupResponse signup(SignupRequest request);

    /**
     * 사용자 자격증명을 검증하고 AccessToken, RefreshToken을 발급합니다.
     * <p>
     * 반환된 {@link LoginResult}의 토큰은 컨트롤러에서 HttpOnly 쿠키로 설정됩니다.
     *
     * @param request 로그인 요청 DTO (username, password)
     * @return 발급된 토큰 및 사용자 기본 정보
     * @throws com.eof.back.global.exception.exceptions.AuthException 자격증명 불일치 또는 비활성 계정인 경우
     */
    LoginResult login(LoginRequest request);

    /**
     * Refresh Token을 검증한 뒤 새로운 AccessToken, RefreshToken을 재발급합니다.
     * <p>
     * RTR(Refresh Token Rotation) 방식을 사용하므로 재발급 시마다 Refresh Token도 교체됩니다.
     * 이전 Refresh Token은 저장소에서 삭제되어 재사용이 불가능합니다.
     *
     * @param refreshToken 쿠키에서 추출한 현재 Refresh Token
     * @return 새로 발급된 토큰 및 사용자 기본 정보
     * @throws com.eof.back.global.exception.exceptions.AuthException 토큰이 유효하지 않거나 만료된 경우
     */
    LoginResult reissue(String refreshToken);

    /**
     * Refresh Token을 검증한 뒤 저장소에서 삭제하여 로그아웃 처리합니다.
     * <p>
     * 저장소에서 Refresh Token이 삭제되면 이후 재발급 요청이 불가능해집니다.
     * 쿠키 삭제는 컨트롤러에서 처리합니다.
     *
     * @param refreshToken 쿠키에서 추출한 현재 Refresh Token
     * @throws com.eof.back.global.exception.exceptions.AuthException 토큰이 유효하지 않은 경우
     */
    void logout(String refreshToken);

    /**
     * 사용자를 탈퇴 처리합니다. (soft delete)
     * <p>
     * 계정 상태를 DELETED로 변경하고 Refresh Token을 저장소에서 삭제합니다.
     * 실제 데이터는 DB에 유지됩니다.
     *
     * @param userId 탈퇴할 사용자 ID
     * @throws com.eof.back.global.exception.exceptions.AuthException 사용자가 없거나 이미 탈퇴한 경우
     */
    void withdraw(Long userId);
}
