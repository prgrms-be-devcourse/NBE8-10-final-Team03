package com.eof.back.domain.user.quizsetbookmark.repository;

import com.eof.back.domain.user.quizsetbookmark.entity.QuizSetBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * {@link QuizSetBookmark} 엔티티에 대한 JPA 레포지토리입니다.
 * <p>
 * 북마크 존재 여부 확인, 삭제, 목록 조회 쿼리를 제공합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link JpaRepository}를 상속받습니다.
 *
 * @author 5h6vm
 * @see QuizSetBookmark
 * @since 2026-03-24
 */
public interface QuizSetBookmarkRepository extends JpaRepository<QuizSetBookmark, Long> {

    @Modifying
    @Query("DELETE FROM QuizSetBookmark b WHERE b.user.id = :userId AND b.quizSet.id = :quizSetId")
    int deleteByUserIdAndQuizSetId(Long userId, Long quizSetId);

    @Query("SELECT b FROM QuizSetBookmark b JOIN FETCH b.quizSet WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<QuizSetBookmark> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
