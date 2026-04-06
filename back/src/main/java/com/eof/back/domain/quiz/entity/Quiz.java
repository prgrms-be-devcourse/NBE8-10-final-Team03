package com.eof.back.domain.quiz.entity;

import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>퀴즈 세트 내에 속한 개별 퀴즈의 상세 데이터를 관리하는 엔티티입니다.</p>
 * 퀴즈의 유형(문제 형태/정답 형태), 발문(내용), 이미지, 유튜브 링크(시작/종료 시간 포함), 그리고 사지선다형 보기를 포함합니다.
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
    private QuizSet quizSet;

    /**
     * 문제의 형태 (텍스트, 이미지, 영상, 음성)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    /**
     * 정답의 제출 형태 (객관식, 주관식)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerType answerType;

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
     * 문제에 표시할 이미지 URL (선택 사항)
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * 유튜브 임베드 링크 (선택 사항, 영상/음성 문제 시 사용)
     */
    @Column(length = 500)
    private String videoUrl;

    /**
     * 유튜브 영상 시작 시간 (초 단위, 선택 사항)
     */
    private Integer startTime;

    /**
     * 유튜브 영상 종료 시간 (초 단위, 선택 사항)
     */
    private Integer endTime;

    /**
     * 첫 번째 보기 내용 (객관식일 경우 필수)
     */
    @Column
    private String choice1;

    /**
     * 두 번째 보기 내용 (객관식일 경우 필수)
     */
    @Column
    private String choice2;

    /**
     * 세 번째 보기 내용 (객관식일 경우 필수)
     */
    @Column
    private String choice3;

    /**
     * 네 번째 보기 내용 (객관식일 경우 필수)
     */
    @Column
    private String choice4;

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param quizSet      소속 세트
     * @param questionType 문제 유형
     * @param answerType   정답 유형
     * @param content      발문
     * @param answer       정답
     * @param imageUrl     이미지 링크
     * @param videoUrl     유튜브 링크
     * @param startTime    영상 시작 시간
     * @param endTime      영상 종료 시간
     * @param choice1      보기1
     * @param choice2      보기2
     * @param choice3      보기3
     * @param choice4      보기4
     */
    @Builder
    private Quiz(QuizSet quizSet, QuestionType questionType, AnswerType answerType, String content, String answer, String imageUrl, String videoUrl, Integer startTime, Integer endTime, String choice1, String choice2, String choice3, String choice4) {
        this.quizSet = quizSet;
        this.questionType = questionType != null ? questionType : QuestionType.TEXT;
        this.answerType = answerType != null ? answerType : AnswerType.MULTIPLE_CHOICE;
        this.content = content;
        this.answer = answer;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.choice3 = choice3;
        this.choice4 = choice4;
    }

    /**
     * 퀴즈의 정보를 수정합니다. (PATCH 목적)
     * null이 아닌 필드만 업데이트합니다.
     *
     * @param questionType 새로운 문제 유형
     * @param answerType   새로운 정답 유형
     * @param content      새로운 발문
     * @param answer       새로운 정답
     * @param imageUrl     새로운 이미지 링크
     * @param videoUrl     새로운 유튜브 링크
     * @param startTime    새로운 시작 시간
     * @param endTime      새로운 종료 시간
     * @param choice1      새로운 보기1
     * @param choice2      새로운 보기2
     * @param choice3      새로운 보기3
     * @param choice4      새로운 보기4
     */
    public void update(QuestionType questionType, AnswerType answerType, String content, String answer, String imageUrl, String videoUrl, Integer startTime, Integer endTime, String choice1, String choice2, String choice3, String choice4) {
        if (questionType != null) this.questionType = questionType;
        if (answerType != null) this.answerType = answerType;
        if (content != null) this.content = content;
        if (answer != null) this.answer = answer;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (videoUrl != null) this.videoUrl = videoUrl;
        if (startTime != null) this.startTime = startTime;
        if (endTime != null) this.endTime = endTime;
        if (choice1 != null) this.choice1 = choice1;
        if (choice2 != null) this.choice2 = choice2;
        if (choice3 != null) this.choice3 = choice3;
        if (choice4 != null) this.choice4 = choice4;
    }
}
