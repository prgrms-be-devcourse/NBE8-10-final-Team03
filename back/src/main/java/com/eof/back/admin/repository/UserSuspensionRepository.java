package com.eof.back.admin.repository;

import com.eof.back.admin.entity.UserSuspension;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link UserSuspension} 엔티티에 대한 데이터 액세스 계층입니다.
 * <p>
 * 사용자의 정지 상세 기록을 데이터베이스와 연동하여 관리하며,
 * 특정 사용자 ID를 기반으로 정지 정보를 조회하거나 삭제하는 기능을 제공합니다.
 * </p>
 *
 * @author MintyU
 * @since 2026-03-31
 */
@Repository
public interface UserSuspensionRepository extends JpaRepository<UserSuspension, Long> {

    /**
     * 특정 사용자 식별자(ID)를 가진 사용자의 정지 상세 정보를 조회합니다.
     *
     * @param userId 조회할 사용자의 고유 식별자
     * @return 발견된 정지 정보가 포함된 Optional 객체
     */
    Optional<UserSuspension> findByUserId(Long userId);

    /**
     * 특정 사용자의 정지 상세 정보를 시스템에서 완전히 제거합니다.
     * 주로 관리자가 정지를 조기에 해제하거나 사용자가 탈퇴할 때 호출됩니다.
     *
     * @param userId 정지 정보를 삭제할 사용자의 고유 식별자
     */
    void deleteByUserId(Long userId);
}
