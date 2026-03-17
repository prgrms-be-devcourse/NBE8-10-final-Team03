package com.eof.back.domain.quizset.entity;

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
 * <p>여러 퀴즈 문제를 하나의 논리적 단위로 묶는 퀴즈 세트 엔티티입니다.</p>
 * 퀴즈 세트의 기본 메타데이터(제목, 설명)와 제작자 정보,
 * 세트 내에 포함된 총 문제의 수 등을 관리합니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
@Entity
@Table(name = "quiz_sets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSet extends BaseEntity {

    /**
     * 사용자가 식별하기 위한 퀴즈 세트의 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 퀴즈 세트의 상세 설명. 문제의 주제나 대상 수준 등을 기재합니다.
     */
    @Column(length = 1000)
    private String description;

    /**
     * 해당 퀴즈 세트를 제작한 사용자 정보입니다.
     * 지연 로딩(LAZY)을 통해 참조 시점에 데이터를 로드합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /**
     * 이 세트에 포함된 총 문제 개수. 퀴즈 세트 조회 시의 요약 정보로 사용됩니다.
     */
    @Column(nullable = false)
    private Integer totalQuestionCount;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param title 제목
     * @param description 설명
     * @param creator 제작자
     * @param totalQuestionCount 총 문제 수
     */
    @Builder
    private QuizSet(String title, String description, User creator, Integer totalQuestionCount) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.totalQuestionCount = totalQuestionCount;
    }

    /**
     * 퀴즈 세트 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param title 퀴즈 세트 제목
     * @param description 퀴즈 세트 설명
     * @param creator 제작자 (User 엔티티)
     * @param totalQuestionCount 세트 내 총 문제 수
     * @return 생성된 QuizSet 엔티티 객체
     */
    public static QuizSet of(String title, String description, User creator, Integer totalQuestionCount) {
        return QuizSet.builder()
                .title(title)
                .description(description)
                .creator(creator)
                .totalQuestionCount(totalQuestionCount)
                .build();
    }
}
