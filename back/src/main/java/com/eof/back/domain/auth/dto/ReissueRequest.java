package com.eof.back.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Access Token 재발급 요청 정보를 담는 DTO입니다.
 * <p>
 * 클라이언트가 보유한 Refresh Token을 서버에 전달하여
 * 새로운 Access Token 및 Refresh Token 발급을 요청할 때 사용됩니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public record ReissueRequest(
        @NotBlank(message = "refreshToken은 비어 있을 수 없습니다.")
        String refreshToken
) {
}
