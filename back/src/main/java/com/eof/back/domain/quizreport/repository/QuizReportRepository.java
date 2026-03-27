package com.eof.back.domain.quizreport.repository;

import com.eof.back.domain.quizreport.entity.QuizReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link QuizReport} 엔티티에 대한 데이터 액세스 계층을 정의합니다.
 * <p>
 * Spring Data JPA를 사용하여 데이터베이스와 통신합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link JpaRepository} 인터페이스를 상속받습니다.
 *
 * <p><b>빈 관리:</b><br>
 * Spring Data JPA에 의해 자동으로 구현체가 생성되고 Bean으로 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data JPA를 사용합니다.
 *
 * @author MintyU
 * @since 2026-03-27
 */
@Repository
public interface QuizReportRepository extends JpaRepository<QuizReport, Long> {
}
