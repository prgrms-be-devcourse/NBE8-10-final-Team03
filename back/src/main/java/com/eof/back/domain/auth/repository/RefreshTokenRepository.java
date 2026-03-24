package com.eof.back.domain.auth.repository;

import com.eof.back.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RefreshToken 엔티티의 영속성 처리를 담당하는 Repository
 * <p>
 * 사용자별로 저장된 Refresh Token을 조회, 저장, 삭제하는 기능을 제공합니다.
 *  * 기본적으로 userId를 기준으로 토큰을 관리하며,
 *  * 로그아웃 시 토큰 삭제, 재로그인 또는 재발급 시 토큰 갱신에 사용됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link JpaRepository}를 상속
 *
 * <p><b>빈 관리:</b><br>
 * Spring Data JPA에 의해 Repository Bean으로 자동 등록됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data JPA를 사용하여 데이터베이스 접근을 처리합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
