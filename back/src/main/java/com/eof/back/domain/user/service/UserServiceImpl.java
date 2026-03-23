package com.eof.back.domain.user.service;

import com.eof.back.domain.user.dto.UserInfoResponse;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import lombok.RequiredArgsConstructor;
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
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
