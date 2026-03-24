package com.eof.back.domain.user.dto;

/**
 * 내 정보 수정 결과를 담는 DTO입니다.
 *
 * <p>수정 완료 후 변경된 사용자의 기본 정보를 클라이언트에 전달합니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-24
 */
public record UserUpdateResponse(
        Long userId,
        String nickname
) {
}
