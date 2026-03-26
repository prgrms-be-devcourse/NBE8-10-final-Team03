package com.eof.back.domain.auth.service;

import com.eof.back.domain.auth.dto.LoginRequest;
import com.eof.back.domain.auth.dto.LoginResponse;
import com.eof.back.domain.auth.dto.ReissueResponse;
import com.eof.back.domain.auth.dto.SignupRequest;
import com.eof.back.domain.auth.dto.SignupResponse;
import com.eof.back.domain.auth.entity.RefreshToken;
import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 관련 비즈니스 로직 구현체입니다.
 * <p>
 * 회원가입, 로그인, Refresh Token 재발급, 로그아웃을 처리합니다.
 * 저장소에 보관된 Refresh Token과 요청 토큰을 비교하여
 * 위변조되었거나 로그아웃된 토큰의 사용을 방지합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link AuthService}를 구현한 서비스 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 의존성 주입을 통해 JWT 처리기, Refresh Token 저장소, 사용자 Repository, 비밀번호 인코더를 주입받습니다.
 *
 * <p><b>빈 관리:</b><br>
 * Spring의 서비스 Bean으로 등록되어 사용됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Framework, Spring Transaction, JWT Provider를 사용합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${custom.jwt.refreshTokenExpirationSeconds}")
    private long refreshTokenExpireSeconds;

    @Override
    public SignupResponse signup(SignupRequest req) {

        // 1. 아이디/닉네임 중복 검증
        if (userRepository.existsByUsername(req.username())) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_EXIST, "중복 아이디: " + req.username());
        }
        if (userRepository.existsByNickname(req.nickname())) {
            throw new AuthException(AuthErrorCode.NICKNAME_ALREADY_EXIST, "중복 닉네임: " + req.nickname());
        }

        // 2. 비밀번호 암호화 및 사용자 생성
        User user = User.of(req.username(), passwordEncoder.encode(req.password()), req.nickname());

        // 3. 저장 (동시성 충돌 처리)
        try {
            User savedUser = userRepository.saveAndFlush(user);
            return new SignupResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getNickname());
        } catch (DataIntegrityViolationException e) {
            throw new AuthException(AuthErrorCode.SIGNUP_FAIL, "회원가입 중 충돌이 발생하였습니다.");
        }
    }

    @Override
    public LoginResponse login(LoginRequest req) {

        // 1. username으로 사용자 조회
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 3. 탈퇴 여부 확인
        if (user.isDeleted()) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_DELETED);
        }

        // 4. AccessToken, RefreshToken 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), user.getRole().name(), user.getNickname());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 4. RefreshToken 저장
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds);
        refreshTokenStore.save(user.getId(), refreshToken, refreshTokenExpiredAt);

        return new LoginResponse(accessToken, refreshToken, user.getId(), user.getNickname());
    }

    @Override
    public ReissueResponse reissue(String refreshToken) {

        // 1. Refresh Token 검증 및 저장된 토큰 조회
        RefreshToken savedRefreshToken = validateAndGetRefreshToken(refreshToken);

        // 2. 사용자 존재 여부 확인
        User user = userRepository.findById(savedRefreshToken.getUserId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 3. 새로운 Access Token, Refresh Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getNickname()
        );
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 4. Refresh Token 저장소 갱신
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds);
        refreshTokenStore.save(user.getId(), newRefreshToken, refreshTokenExpiredAt);

        return new ReissueResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String refreshToken) {

        // 1. Refresh Token 검증 및 저장된 토큰 조회
        RefreshToken savedRefreshToken = validateAndGetRefreshToken(refreshToken);

        // 2. 저장소에서 Refresh Token 삭제
        refreshTokenStore.delete(savedRefreshToken.getUserId());
    }

    @Override
    public void withdraw(Long userId) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 2. 이미 탈퇴한 사용자 검증
        if (user.isDeleted()) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_DELETED);
        }

        // 3. soft delete 처리
        user.delete();

        // 4. Refresh Token 삭제
        refreshTokenStore.delete(userId);
    }

    private RefreshToken validateAndGetRefreshToken(String refreshToken) {

        // JWT 서명 및 만료 검증
        Claims claims = jwtTokenProvider.validateToken(refreshToken);
        Long userId = jwtTokenProvider.getUserId(claims);

        // DB에 저장된 Refresh Token 조회
        RefreshToken savedRefreshToken = refreshTokenStore.findByUserId(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_INVALID));

        // DB 저장 만료 시간 검증
        if (savedRefreshToken.isExpired()) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        }

        // 저장된 토큰과 요청 토큰 비교 (재사용/위변조 방지)
        if (!savedRefreshToken.getToken().equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        return savedRefreshToken;
    }
}
