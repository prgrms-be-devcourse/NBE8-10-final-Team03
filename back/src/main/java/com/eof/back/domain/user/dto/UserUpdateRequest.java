package com.eof.back.domain.user.dto;

/**
 * 내 정보 수정 요청 데이터를 담는 DTO입니다.
 *
 * <p>닉네임과 비밀번호를 선택적으로 수정할 수 있으며, null인 항목은 변경되지 않습니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-24
 */
public record UserUpdateRequest(
        String nickname,
        String password
) {
}
