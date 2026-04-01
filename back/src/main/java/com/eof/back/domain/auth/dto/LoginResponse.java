package com.eof.back.domain.auth.dto;

/**
 * 로그인 결과 응답 데이터를 전달하는 DTO입니다.
 *
 * <p>로그인이 성공적으로 완료된 후 클라이언트에게 반환되는 정보를 담습니다.</p>
 *
 * <p>토큰은 HttpOnly 쿠키로 전달되므로 응답 body에는 포함되지 않습니다.</p>
 *
 * <p>주요 목적:</p>
 * <ul>
 *     <li>클라이언트에서 사용자 식별에 필요한 기본 정보 반환</li>
 * </ul>
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public record LoginResponse(
        Long userId,
        String nickname,
        String role
) {
}
