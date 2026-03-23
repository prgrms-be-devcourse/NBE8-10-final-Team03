package com.eof.back.domain.user.service;

import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.domain.user.dto.*;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 사용자 도메인과 관련된 비즈니스 로직을 처리하는 서비스입니다.
 *
 * {@link UserService}의 구현체로,
 * 사용자 도메인과 관련된 비즈니스 로직을 처리합니다.
 *
 * <p><b>주요 기능:</b><br>
 * - 회원가입 (아이디/닉네임 중복 검증, 비밀번호 암호화, 동시성 충돌 처리)
 * - 로그인 (비밀번호 검증, AccessToken/RefreshToken 발급)
 * - 내 정보 조회 (사용자 ID로 조회)
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    @Value("${custom.jwt.refreshTokenExpirationSeconds}")
    private long refreshTokenExpireSeconds;

    @Override
    @Transactional
    public UserSignupResponse signup(UserSignupRequest req) {
        validateDuplicateUsername(req.username());
        validateDuplicateNickname(req.nickname());

        User user = User.of(req.username(), passwordEncoder.encode(req.password()), req.nickname());

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return new UserSignupResponse(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getNickname()
            );
        } catch (DataIntegrityViolationException e) {
            throw new AuthException(AuthErrorCode.SIGNUP_FAIL, "회원가입 중 충돌이 발생하였습니다.");
        }
    }

    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_EXIST, "중복 아이디: " + username);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new AuthException(AuthErrorCode.NICKNAME_ALREADY_EXIST, "중복 닉네임: " + nickname);
        }
    }


    @Override
    @Transactional
    public UserLoginResponse login(UserLoginRequest req) {

        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds);
        refreshTokenStore.save(user.getId(), refreshToken, refreshTokenExpiredAt);

        return new UserLoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getNickname()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}
