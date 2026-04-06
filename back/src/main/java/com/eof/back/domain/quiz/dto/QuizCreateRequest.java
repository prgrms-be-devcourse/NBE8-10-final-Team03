package com.eof.back.domain.quiz.dto;

import com.eof.back.domain.quiz.entity.AnswerType;
import com.eof.back.domain.quiz.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 새로운 퀴즈 생성을 위한 요청 데이터 전송 객체(DTO)입니다.
 * <p>
 * 퀴즈의 내용, 유형, 정답, 이미지/유튜브 링크(시간 포함), 그리고 4개의 선택지 정보를 포함합니다.
 *
 * @param questionType 문제 유형 (텍스트, 이미지, 영상, 음성)
 * @param answerType   정답 유형 (객관식, 주관식)
 * @param content      퀴즈 문제 내용 (최대 2000자)
 * @param answer       퀴즈의 정답 내용
 * @param imageUrl     이미지 링크
 * @param videoUrl     유튜브 임베드 링크
 * @param startTime    유튜브 시작 시간
 * @param endTime      유튜브 종료 시간
 * @param choice1      첫 번째 선택지 (객관식 필수)
 * @param choice2      두 번째 선택지 (객관식 필수)
 * @param choice3      세 번째 선택지 (객관식 필수)
 * @param choice4      네 번째 선택지 (객관식 필수)
 * @author MintyU
 * @since 2026-03-24
 */
public record QuizCreateRequest(
        @NotNull(message = "문제 유형은 필수입니다.")
        QuestionType questionType,

        @NotNull(message = "정답 유형은 필수입니다.")
        AnswerType answerType,

        @NotBlank(message = "퀴즈 내용은 필수입니다.")
        @Size(max = 2000, message = "퀴즈 내용은 2000자를 초과할 수 없습니다.")
        String content,

        @NotBlank(message = "정답은 필수입니다.")
        String answer,

        String imageUrl,

        String videoUrl,

        Integer startTime,

        Integer endTime,

        String choice1,

        String choice2,

        String choice3,

        String choice4
) {
}
