package com.eof.back.domain.image.repository;

import com.eof.back.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 이미지 엔티티에 대한 데이터 접근을 담당하는 리포지토리입니다.
 * <p>
 * Spring Data JPA의 JpaRepository를 활용하여 기본적인 CRUD 기능을 제공하며, 이미지의 영속성 관리를 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link org.springframework.data.jpa.repository.JpaRepository} 를 상속받습니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link org.springframework.stereotype.Repository} 어노테이션을 통해 스프링 빈으로 관리됩니다.
 *
 * @author Minji-032
 * @since 2026-04-06
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * 접근 URL로 이미지를 조회합니다.
     *
     * @param accessUrl S3 접근 URL
     * @return 해당 URL의 Image, 없으면 empty
     */
    Optional<Image> findByAccessUrl(String accessUrl);

    /**
     * 접근 URL로 이미지를 uploader와 함께 조회합니다.
     * 삭제 권한 검증 시 uploader 프록시 초기화로 인한 추가 쿼리를 방지합니다.
     *
     * @param accessUrl S3 접근 URL
     * @return uploader가 즉시 로딩된 Image, 없으면 empty
     */
    @Query("SELECT i FROM Image i JOIN FETCH i.uploader WHERE i.accessUrl = :accessUrl")
    Optional<Image> findByAccessUrlWithUploader(@Param("accessUrl") String accessUrl);
}
