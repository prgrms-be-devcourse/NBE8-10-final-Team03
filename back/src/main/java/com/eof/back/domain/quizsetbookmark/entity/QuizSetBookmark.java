package com.eof.back.domain.quizsetbookmark.entity;

import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.user.entity.User;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>사용자가 특정 퀴즈 세트를 다시 찾기 위해 등록한 즐겨찾기 정보를 관리하는 엔티티입니다.</p>
 * 특정 사용자와 퀴즈 세트 간의 다대다(N:M) 관계를 일대다(1:N) 관계로 풀어낸 연결 테이블 성격을 가지며,
 * 동일한 사용자가 동일한 세트를 중복하여 즐겨찾기할 수 없도록 유니크 제약 조건을 가집니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
@Entity
@Table(
    name = "quiz_set_bookmarks",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_quiz_set",
            columnNames = {"user_id", "quiz_set_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSetBookmark extends BaseEntity {

    /**
     * 즐겨찾기를 등록한 사용자 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 사용자가 선택하여 보관하려는 대상 퀴즈 세트 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param user 사용자
     * @param quizSet 대상 세트
     */
    @Builder
    private QuizSetBookmark(User user, QuizSet quizSet) {
        this.user = user;
        this.quizSet = quizSet;
    }

    /**
     * QuizSetBookmark 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param user 즐겨찾기를 등록하는 사용자 (User 엔티티)
     * @param quizSet 즐겨찾기 대상이 되는 세트 (QuizSet 엔티티)
     * @return 생성된 QuizSetBookmark 엔티티 객체
     */
    public static QuizSetBookmark of(User user, QuizSet quizSet) {
        return QuizSetBookmark.builder()
                .user(user)
                .quizSet(quizSet)
                .build();
    }
}
