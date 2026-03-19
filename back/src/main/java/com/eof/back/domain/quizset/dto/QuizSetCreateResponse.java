package com.eof.back.domain.quizset.dto;

import com.eof.back.domain.quizset.entity.QuizSet;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 세트 생성 성공 시 반환되는 응답 데이터 전송 객체(DTO)입니다.
 * <p>
 * 생성된 퀴즈 세트의 영속성 상태 정보를 클라이언트에게 전달하며, 엔티티를 직접 노출하지 않도록 보호합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@link lombok.Builder} 를 활용하여 인스턴스를 생성합니다. <br>
 * {@link #from(QuizSet)} 정적 팩토리 메서드를 통해 엔티티를 DTO로 변환합니다. <br>
 *
 * @author MintyU
 * @since 2026-03-19
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QuizSetCreateResponse {

    private Long id;
    private String title;
    private String description;
    private String creatorNickname;
    private Integer totalQuestionCount;
    private LocalDateTime createdAt;

    /**
     * QuizSet 엔티티로부터 QuizSetCreateResponse DTO를 생성합니다.
     *
     * @param quizSet QuizSet 엔티티
     * @return 생성된 QuizSetCreateResponse 객체
     */
    public static QuizSetCreateResponse from(QuizSet quizSet) {
        return QuizSetCreateResponse.builder()
                .id(quizSet.getId())
                .title(quizSet.getTitle())
                .description(quizSet.getDescription())
                .creatorNickname(quizSet.getCreator().getNickname())
                .totalQuestionCount(quizSet.getTotalQuizCount())
                .createdAt(quizSet.getCreatedAt())
                .build();
    }
}
