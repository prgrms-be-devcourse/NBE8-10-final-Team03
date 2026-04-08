package com.eof.back.domain.quizset.entity;

import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여러 퀴즈를 하나의 논리적 단위로 묶는 퀴즈 세트 엔티티입니다.
 * <p>
 * 퀴즈 세트의 기본 메타데이터(제목, 설명)와 제작자 정보, 세트 내에 포함된 총 퀴즈의 수 등을 관리하며 데이터베이스의 quiz_sets 테이블과 매핑됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseEntity}를 상속받아 생성 및 수정 시간을 자동으로 관리합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@link #QuizSet(String, String, User, Integer, List)} <br>
 * 빌더 패턴을 통해 제목, 설명, 제작자, 총 퀴즈 수를 입력받아 인스턴스를 생성합니다. <br>
 *
 * @author MintyU
 * @since 2026-03-19
 */
@Entity
@Table(name = "quiz_sets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSet extends BaseEntity {

    /**
     * 사용자가 식별하기 위한 퀴즈 세트의 제목
     */
    @Column(nullable = false, length = 30)
    private String title;

    /**
     * 퀴즈 세트의 상세 설명. 퀴즈의 주제나 대상 수준 등을 기재합니다.
     */
    @Column(length = 255)
    private String description;

    /**
     * 해당 퀴즈 세트를 제작한 사용자 정보입니다.
     * 지연 로딩(LAZY)을 통해 참조 시점에 데이터를 로드합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /**
     * 이 세트에 포함된 총 퀴즈 개수. 퀴즈 세트 조회 시의 요약 정보로 사용됩니다.
     */
    @Column(nullable = false)
    private Integer totalQuizCount = 0;

    /**
     * 퀴즈 세트의 썸네일 이미지 URL입니다.
     */
    @Column(length = 500)
    private String thumbnailUrl;

    /**
     * 이 퀴즈 세트에 포함된 개별 퀴즈 목록입니다.
     */
    @OneToMany(mappedBy = "quizSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

    /**
     * 빌더 패턴을 이용한 생성자입니다.
     *
     * @param title 제목
     * @param description 설명
     * @param creator 제작자
     * @param totalQuizCount 총 퀴즈 수
     * @param quizzes 퀴즈 목록
     */
    @Builder
    private QuizSet(String title, String description, String thumbnailUrl, User creator, Integer totalQuizCount, List<Quiz> quizzes) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.creator = creator;
        this.totalQuizCount = (totalQuizCount != null) ? totalQuizCount : 0;
        if (quizzes != null) {
            this.quizzes = quizzes;
        }
    }

    /**
     * 퀴즈 세트 생성을 위한 정적 팩토리 메서드입니다.
     *
     * @param title 퀴즈 세트 제목
     * @param description 퀴즈 세트 설명
     * @param creator 제작자 (User 엔티티)
     * @return 생성된 QuizSet 엔티티 객체
     */
    public static QuizSet of(String title, String description, User creator) {
        return QuizSet.builder()
                .title(title)
                .description(description)
                .creator(creator)
                .totalQuizCount(0)
                .build();
    }

    /**
     * 퀴즈 세트의 정보를 수정합니다. (PATCH 목적)
     * null이 아닌 필드만 업데이트합니다.
     *
     * @param title 새로운 제목
     * @param description 새로운 설명
     */
    public void update(String title, String description, String thumbnailUrl) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    /**
     * 퀴즈가 추가될 때 총 퀴즈 수를 1 증가시킵니다.
     */
    public void increaseQuizCount() {
        this.totalQuizCount++;
    }

    /**
     * 퀴즈가 삭제될 때 총 퀴즈 수를 1 감소시킵니다.
     * 최소값은 0을 유지합니다.
     */
    public void decreaseQuizCount() {
        if (this.totalQuizCount > 0) {
            this.totalQuizCount--;
        }
    }
}
