package com.eof.back.domain.quizset.repository;

import com.eof.back.domain.quizset.entity.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 퀴즈 세트(QuizSet) 엔티티에 대한 데이터 접근을 담당하는 리포지토리입니다.
 * <p>
 * Spring Data JPA의 JpaRepository를 활용하여 기본적인 CRUD 기능을 제공하며, 퀴즈 세트의 영속성 관리를 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link org.springframework.data.jpa.repository.JpaRepository} 를 상속받습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@link org.springframework.stereotype.Repository} 어노테이션을 통해 스프링 빈으로 관리됩니다. <br>
 *
 * @author MintyU
 * @since 2026-03-19
 */
@Repository
public interface QuizSetRepository extends JpaRepository<QuizSet, Long> {

    @Modifying
    @Query("DELETE FROM QuizSet q WHERE q.id = :id AND q.creator.id = :userId")
    int deleteByIdAndCreatorId(@Param("id") Long id, @Param("userId") Long userId);
}
