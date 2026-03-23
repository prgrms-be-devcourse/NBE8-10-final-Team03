package com.eof.back.domain.auth.service;

import com.eof.back.domain.auth.dto.ReissueResponse;
import com.eof.back.domain.auth.entity.RefreshToken;
import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 관련 비즈니스 로직 구현체입니다.
 * <p>
 * Refresh Token 검증을 기반으로 Access Token 및 Refresh Token 재발급을 처리합니다.
 * 저장소에 보관된 Refresh Token과 요청 토큰을 비교하여
 * 위변조되었거나 로그아웃된 토큰의 사용을 방지합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link AuthService}를 구현한 서비스 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 의존성 주입을 통해 JWT 처리기, Refresh Token 저장소, 사용자 Repository를 주입받습니다.
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

    @Value("${custom.jwt.refresh-token-expire-seconds}")
    private long refreshTokenExpireSeconds;

    @Override
    public ReissueResponse reissue(String refreshToken) {

        // 1. JWT 서명 및 만료 검증 → 유효하지 않으면 예외 발생
        Claims claims = jwtTokenProvider.validateToken(refreshToken);

        // 2. Claims에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserId(claims);

        // 3. DB에 저장된 Refresh Token 조회
        RefreshToken savedRefreshToken = refreshTokenStore.findByUserId(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_INVALID));

        // 4. 저장된 토큰과 요청 토큰 비교 (재사용/위변조 방지)
        if (!savedRefreshToken.getToken().equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        // 5. 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        // 6. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        // 7. 새로운 Refresh Token 생성
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 8. Refresh Token 만료 시간 계산 및 저장소 갱신
        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpireSeconds);
        refreshTokenStore.save(user.getId(), newRefreshToken, refreshTokenExpiredAt);

        // 9. 새 토큰 반환
        return new ReissueResponse(newAccessToken, newRefreshToken);
    }
}
