package com.eof.back.domain.questionreview.entity;

import com.eof.back.domain.question.entity.Question;
import com.eof.back.domain.user.entity.User;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>문항의 품질을 평가하기 위해 사용자가 작성한 리뷰와 별점을 관리하는 엔티티입니다.</p>
 * 퀴즈 제작자에게는 피드백을 제공하고, 다른 사용자들에게는 문제의 신뢰도를
 * 판단할 수 있는 지표로 활용됩니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
@Entity
@Table(name = "question_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionReview extends BaseEntity {

    /**
     * 리뷰를 작성한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    /**
     * 리뷰 대상이 되는 문항
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 사용자가 입력한 구체적인 피드백 내용
     */
    @Column(nullable = false, length = 1000)
    private String content;

    /**
     * 문제에 대한 만족도를 점수화한 별점. (일반적으로 1~5점 사이의 정수값)
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param writer 작성자
     * @param question 대상 문제
     * @param content 리뷰 내용
     * @param rating 별점
     */
    @Builder
    private QuestionReview(User writer, Question question, String content, Integer rating) {
        this.writer = writer;
        this.question = question;
        this.content = content;
        this.rating = rating;
    }

    /**
     * QuestionReview 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param writer 리뷰를 작성하는 사용자 (User 엔티티)
     * @param question 대상 문항 (Question 엔티티)
     * @param content 피드백 내용
     * @param rating 별점 (1~5 정수)
     * @return 생성된 QuestionReview 엔티티 객체
     */
    public static QuestionReview of(User writer, Question question, String content, Integer rating) {
        return QuestionReview.builder()
                .writer(writer)
                .question(question)
                .content(content)
                .rating(rating)
                .build();
    }
}
