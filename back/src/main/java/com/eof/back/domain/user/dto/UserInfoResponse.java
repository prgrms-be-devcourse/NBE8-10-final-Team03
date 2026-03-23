package com.eof.back.domain.user.dto;

/**
 * 내 정보 조회 응답 데이터를 담는 DTO입니다.
 *
 * <p>로그인한 사용자의 기본 정보를 클라이언트에 전달할 때 사용합니다.</p>
 *
 * <p><b>포함 정보:</b><br>
 * - 사용자 ID
 * - 아이디(username)
 * - 닉네임
 * - 권한(role)
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public record UserInfoResponse(
        Long id,
        String username,
        String nickname,
        String role
) {
}
