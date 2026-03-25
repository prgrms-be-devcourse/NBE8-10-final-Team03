package com.eof.back.domain.user.quizsetbookmark.repository;

import com.eof.back.domain.user.quizsetbookmark.entity.QuizSetBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author 5h6vm
 * @see
 * @since 2026-03-24
 */
public interface QuizSetBookmarkRepository extends JpaRepository<QuizSetBookmark, Long> {

    boolean existsByUserIdAndQuizSetId(Long userId, Long quizSetId);

    @Modifying
    @Query("DELETE FROM QuizSetBookmark b WHERE b.user.id = :userId AND b.quizSet.id = :quizSetId")
    int deleteByUserIdAndQuizSetId(Long userId, Long quizSetId);

    List<QuizSetBookmark> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
