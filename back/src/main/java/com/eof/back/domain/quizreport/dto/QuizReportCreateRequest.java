package com.eof.back.domain.quizreport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 신고 생성을 위한 요청 데이터 전송 객체입니다.
 * <p>
 * 사용자가 입력한 신고 정보를 전달받는 데 사용됩니다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@code QuizReportCreateRequest(Long quizSetId, String reason)} <br>
 * 신고 정보를 초기화하는 생성자입니다. <br>
 *
 * <p><b>외부 모듈:</b><br>
 * Bean Validation(Jakarta Validation) API를 사용합니다.
 *
 * @author MintyU
 * @since 2026-03-27
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizReportCreateRequest {

    /**
     * 신고할 퀴즈 세트의 식별자
     */
    @NotNull(message = "신고할 퀴즈 세트 ID는 필수입니다.")
    private Long quizSetId;

    /**
     * 상세 신고 사유
     */
    @NotBlank(message = "신고 사유를 입력해 주세요.")
    private String reason;

    /**
     * 생성자
     *
     * @param quizSetId 퀴즈 세트 ID
     * @param reason 신고 사유
     */
    public QuizReportCreateRequest(Long quizSetId, String reason) {
        this.quizSetId = quizSetId;
        this.reason = reason;
    }
}
