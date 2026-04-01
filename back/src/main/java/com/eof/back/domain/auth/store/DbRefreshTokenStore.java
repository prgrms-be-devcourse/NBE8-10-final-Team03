package com.eof.back.domain.auth.store;

import com.eof.back.domain.auth.entity.RefreshToken;
import com.eof.back.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DB 기반 Refresh Token 저장소 구현체입니다.
 * <p>
 * RefreshTokenStore 인터페이스를 구현하며,
 * Refresh Token의 저장, 조회, 삭제 작업을 JPA Repository를 통해 수행합니다.
 * <p>
 * 현재는 관계형 데이터베이스를 사용하여 Refresh Token을 관리하며,
 * 추후 Redis 등 다른 저장소로 변경되더라도 서비스 계층의 변경을 최소화할 수 있도록
 * 저장소 접근 로직을 분리한 구현체입니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link RefreshTokenStore}를 구현한 DB 기반 저장소 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code DbRefreshTokenStore(RefreshTokenRepository refreshTokenRepository)} <br>
 * RefreshTokenRepository를 주입받아 DB 접근 로직을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Spring의 컴포넌트 스캔 대상이며, Bean으로 등록되어 주입됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data JPA를 사용하여 Refresh Token 엔티티를 관리합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
@Component
@ConditionalOnProperty(name = "custom.refresh-token.store", havingValue = "db", matchIfMissing = true)
@RequiredArgsConstructor
public class DbRefreshTokenStore implements RefreshTokenStore{

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public void save(Long userId, String token, LocalDateTime expiredAt) {
        refreshTokenRepository.findById(userId)
                .ifPresentOrElse(
                        savedRefreshToken -> savedRefreshToken.updateToken(token, expiredAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(userId)
                                        .token(token)
                                        .expiredAt(expiredAt)
                                        .build()
                        )
                );
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        return refreshTokenRepository.findById(userId);
    }

    @Override
    public void delete(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }
}
