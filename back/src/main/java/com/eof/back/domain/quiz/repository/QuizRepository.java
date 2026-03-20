package com.eof.back.domain.quiz.repository;

import com.eof.back.domain.quizset.entity.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 퀴즈 엔티티에 대한 데이터 접근을 담당하는 리포지토리입니다.
 * <p>
 * Spring Data JPA의 JpaRepository를 활용하여 기본적인 CRUD 기능을 제공하며, 퀴즈의 영속성 관리를 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link org.springframework.data.jpa.repository.JpaRepository} 를 상속받습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@link org.springframework.stereotype.Repository} 어노테이션을 통해 스프링 빈으로 관리됩니다. <br>
 *
 * @author MintyU
 * @since 2026-03-20
 */
public interface QuizRepository extends JpaRepository<QuizSet, Long> {
}
