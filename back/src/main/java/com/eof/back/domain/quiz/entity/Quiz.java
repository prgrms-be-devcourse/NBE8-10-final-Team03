package com.eof.back.domain.quiz.entity;

import com.eof.back.domain.quizset.entity.QuizSet;
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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * <p>퀴즈 세트 내에 속한 개별 퀴즈의 상세 데이터를 관리하는 엔티티입니다.</p>
 * 퀴즈의 발문(내용), 정답, 그리고 사지선다형 보기를 포함합니다.
 *
 * @author MintyU
 * @since 2026-03-18
 */
@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {

    /**
     * 이 퀴즈가 속한 퀴즈 세트입니다.
     * 지연 로딩(LAZY)을 통해 소속 정보를 관리합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QuizSet quizSet;

    /**
     * 실제 퀴즈의 지문 또는 발문 내용
     */
    @Column(nullable = false, length = 2000)
    private String content;

    /**
     * 퀴즈의 정답입니다. 보기에 적힌 텍스트와 일치해야 합니다.
     */
    @Column(nullable = false)
    private String answer;

    /**
     * 첫 번째 보기 내용
     */
    @Column(nullable = false)
    private String choice1;

    /**
     * 두 번째 보기 내용
     */
    @Column(nullable = false)
    private String choice2;

    /**
     * 세 번째 보기 내용
     */
    @Column(nullable = false)
    private String choice3;

    /**
     * 네 번째 보기 내용
     */
    @Column(nullable = false)
    private String choice4;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param quizSet 소속 세트
     * @param content 발문
     * @param answer 정답
     * @param choice1 보기1
     * @param choice2 보기2
     * @param choice3 보기3
     * @param choice4 보기4
     */
    @Builder
    private Quiz(QuizSet quizSet, String content, String answer, String choice1, String choice2, String choice3, String choice4) {
        this.quizSet = quizSet;
        this.content = content;
        this.answer = answer;
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.choice3 = choice3;
        this.choice4 = choice4;
    }

    /**
     * Quiz 엔티티 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param quizSet 소속 퀴즈 세트 (QuizSet 엔티티)
     * @param content 퀴즈 발문 내용
     * @param answer 정답 텍스트
     * @param choice1 선택지 1
     * @param choice2 선택지 2
     * @param choice3 선택지 3
     * @param choice4 선택지 4
     * @return 생성된 Quiz 엔티티 객체
     */
    /**
     * 퀴즈의 정보를 수정합니다. (PATCH 목적)
     * null이 아닌 필드만 업데이트합니다.
     *
     * @param content 새로운 발문
     * @param answer 새로운 정답
     * @param choice1 새로운 보기1
     * @param choice2 새로운 보기2
     * @param choice3 새로운 보기3
     * @param choice4 새로운 보기4
     */
    public void update(String content, String answer, String choice1, String choice2, String choice3, String choice4) {
        if (content != null) this.content = content;
        if (answer != null) this.answer = answer;
        if (choice1 != null) this.choice1 = choice1;
        if (choice2 != null) this.choice2 = choice2;
        if (choice3 != null) this.choice3 = choice3;
        if (choice4 != null) this.choice4 = choice4;
    }
}
