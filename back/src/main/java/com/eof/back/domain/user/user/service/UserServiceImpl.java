package com.eof.back.domain.user.user.service;

import com.eof.back.domain.user.user.dto.UserInfoResponse;
import com.eof.back.domain.user.user.dto.UserUpdateRequest;
import com.eof.back.domain.user.user.dto.UserUpdateResponse;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 도메인과 관련된 비즈니스 로직을 처리하는 서비스입니다.
 *
 * {@link UserService}의 구현체로,
 * 사용자 도메인과 관련된 비즈니스 로직을 처리합니다.
 *
 * <p><b>주요 기능:</b><br>
 * - 내 정보 조회 (사용자 ID로 조회)
 * - 내 정보 수정
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole().name()
        );
    }

    @Override
    @Transactional
    public UserUpdateResponse updateInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        updateNicknameIfPresent(user, request.nickname());
        updatePasswordIfPresent(user, request.password());

        return new UserUpdateResponse(
                user.getId(),
                user.getNickname()
        );
    }
    private void updateNicknameIfPresent(User user, String nickname) {
        if (nickname == null) {
            return;
        }

        if (!user.getNickname().equals(nickname)
                && userRepository.existsByNickname(nickname)) {
            throw new AuthException(AuthErrorCode.NICKNAME_ALREADY_EXIST);
        }

        user.updateNickname(nickname);
    }

    private void updatePasswordIfPresent(User user, String password) {
        if (password == null) {
            return;
        }

        user.updatePassword(passwordEncoder.encode(password));
    }
}

