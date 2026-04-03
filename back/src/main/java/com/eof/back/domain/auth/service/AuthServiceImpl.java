package com.eof.back.domain.auth.service;

import com.eof.back.domain.auth.dto.LoginRequest;
import com.eof.back.domain.auth.dto.LoginResult;
import com.eof.back.domain.auth.dto.SignupRequest;
import com.eof.back.domain.auth.dto.SignupResponse;
import com.eof.back.domain.auth.entity.RefreshToken;
import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.token.TokenVersionStore;
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
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenVersionStore tokenVersionStore;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;

    @Value("${custom.jwt.refreshTokenExpirationSeconds}")
    private long refreshTokenExpireSeconds;

    @Override
    @Transactional
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
    @Transactional
    public LoginResult login(LoginRequest req) {

        // 1. 실패 횟수 조회 (잠금 여부 + CAPTCHA 임계값 체크에 공통 사용)
        int failCount = loginAttemptService.getAttemptCount(req.username());

        if (failCount >= LoginAttemptService.MAX_ATTEMPTS) {
            throw new AuthException(AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED);
        }

        // 2. 실패 횟수가 임계값 이상이면 CAPTCHA 검증
        if (failCount >= CaptchaService.CAPTCHA_THRESHOLD) {
            if (req.captchaToken() == null) {
                throw new AuthException(AuthErrorCode.CAPTCHA_REQUIRED);
            }
            try {
                captchaService.verify(req.captchaToken());
            } catch (AuthException e) {
                loginAttemptService.recordFailure(req.username());
                throw e;
            }
        }

        // 3. username으로 사용자 조회
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        // 4. 비밀번호 검증
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            loginAttemptService.recordFailure(req.username());
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 5. 탈퇴/정지 여부 확인 (구체적인 상태를 노출하지 않아 계정 열거 공격 방지)
        if (!user.isActive()) {
            throw new AuthException(AuthErrorCode.LOGIN_FAIL);
        }

        // 6. 로그인 성공 시 실패 카운터 초기화
        loginAttemptService.resetAttempts(req.username());

        return issueTokens(user);
    }

    @Override
    @Transactional
    public LoginResult reissue(String refreshToken) {

        // 1. Refresh Token 검증 및 Claims 추출
        Claims claims = jwtTokenProvider.validateToken(refreshToken);
        Long userId = jwtTokenProvider.getUserId(claims);
        String username = jwtTokenProvider.getUsername(claims);
        String role = jwtTokenProvider.getRole(claims);
        String nickname = jwtTokenProvider.getNickname(claims);

        // 2. 저장소에서 토큰 조회 및 유효성 검증
        RefreshToken savedRefreshToken = refreshTokenStore.findByUserId(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_INVALID));

        if (savedRefreshToken.isExpired()) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
        }

        if (!savedRefreshToken.getToken().equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        // 3. tokenVersion 검증 및 새 토큰 발급
        // 재발급은 동일 세션 내 연장이므로 version을 증가시키지 않고 현재 값을 그대로 사용합니다.
        // refresh token의 version claim과 Redis 저장 version을 비교하여
        // 다른 기기에서 재로그인한 경우(version 불일치)에는 재발급을 거부합니다.
        long refreshTokenVersion = jwtTokenProvider.getTokenVersion(claims);
        long storedVersion = tokenVersionStore.findByUserId(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_INVALID));
        if (refreshTokenVersion != storedVersion) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, username, role, nickname, storedVersion);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, username, role, nickname, storedVersion);

        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds);
        refreshTokenStore.save(userId, newRefreshToken, refreshTokenExpiredAt);

        return new LoginResult(newAccessToken, newRefreshToken, userId, nickname, role);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {

        // 1. Refresh Token 검증 및 저장된 토큰 조회
        RefreshToken savedRefreshToken = validateAndGetRefreshToken(refreshToken);

        // 2. 저장소에서 Refresh Token 삭제 + tokenVersion 증가 (삭제하면 재로그인 시 version=1 재사용 취약점 발생)
        refreshTokenStore.delete(savedRefreshToken.getUserId());
        tokenVersionStore.increment(savedRefreshToken.getUserId());
    }

    @Override
    @Transactional
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

        // 4. Refresh Token + tokenVersion 삭제
        refreshTokenStore.delete(userId);
        tokenVersionStore.delete(userId);
    }

    /**
     * AccessToken, RefreshToken을 발급하고 저장소에 저장한 뒤 LoginResult로 반환합니다.
     * login과 reissue에서 공통으로 사용됩니다.
     */
    private LoginResult issueTokens(User user) {
        // 로그인 시 version 증가 → 이전 세션의 access token 즉시 무효화 (1계정 1세션)
        long tokenVersion = tokenVersionStore.increment(user.getId());
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getUsername(), user.getRole().name(), user.getNickname(), tokenVersion);
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(), user.getUsername(), user.getRole().name(), user.getNickname(), tokenVersion);

        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds);
        refreshTokenStore.save(user.getId(), refreshToken, refreshTokenExpiredAt);

        return new LoginResult(accessToken, refreshToken, user.getId(), user.getNickname(), user.getRole().name());
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
