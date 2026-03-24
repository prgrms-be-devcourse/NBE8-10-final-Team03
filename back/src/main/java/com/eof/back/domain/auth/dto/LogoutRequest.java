package com.eof.back.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그아웃 요청 정보를 담는 DTO입니다.
 * <p>
 * 클라이언트가 로그아웃 시 서버에 전달하는 Refresh Token 값을 포함합니다.
 * 서버는 해당 토큰을 검증한 뒤 저장소에서 삭제하여 더 이상 재발급에 사용할 수 없도록 처리합니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public record LogoutRequest(
        @NotBlank(message = "refreshToken은 비어 있을 수 없습니다.")
        String refreshToken
) {
}