package com.eof.back.domain.auth.dto;

/**
 * Access Token 재발급 응답 정보를 담는 DTO입니다.
 * <p>
 * 서버가 Refresh Token 검증에 성공한 경우,
 * 새로 발급한 Access Token과 Refresh Token을 클라이언트에 전달할 때 사용됩니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public record ReissueResponse(
        String accessToken,
        String refreshToken
) {
}
