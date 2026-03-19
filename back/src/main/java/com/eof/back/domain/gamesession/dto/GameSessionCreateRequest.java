package com.eof.back.domain.gamesession.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 클라이언트로부터 새로운 게임 세션(방) 생성 요청 데이터를 전달받는 DTO(Data Transfer Object)입니다.
 * <p>
 * 프론트엔드에서 전송된 JSON 형태의 방 생성 데이터를 자바 객체로 매핑합니다.
 * 컨트롤러 계층에 도달하기 전, {@code jakarta.validation} 어노테이션을 사용하여
 * 필수 값 누락 여부나 최소 인원수 등의 데이터 유효성을 1차적으로 검증합니다.
 *
 * @author 유재원
 * @since 2026-03-18
 */
public record GameSessionCreateRequest(

        @NotBlank(message = "방 제목은 필수입니다.")
        String roomName,

        @NotNull(message = "퀴즈 세트 ID는 필수입니다.")
        Long quizSetId,

        @NotNull(message = "최대 인원수는 필수입니다.")
        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        Integer maxPlayers,

        @NotNull(message = "최대 문제 수는 필수입니다.")
        @Min(value = 1, message = "최소 1문제 이상이어야 합니다.")
        Integer maxQuizzes
) {
}