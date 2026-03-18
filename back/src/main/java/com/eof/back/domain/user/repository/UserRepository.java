package com.eof.back.domain.user.repository;

import com.eof.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 User 엔티티에 대한 데이터 접근을 담당하는 Repository입니다.
 *
 * <p>Spring Data JPA의 {@link JpaRepository}를 상속받아 기본적인 CRUD 기능을 제공합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *     <li></li>
 * </ul>
 *
 * <p>인증 및 회원 관련 서비스에서 사용자 정보를 조회할 때 사용됩니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
public interface UserRepository extends JpaRepository<User,Long> {

}
