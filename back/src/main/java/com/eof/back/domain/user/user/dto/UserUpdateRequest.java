package com.eof.back.domain.user.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 내 정보 수정 요청 데이터를 담는 DTO입니다.
 *
 * <p>닉네임과 비밀번호를 선택적으로 수정할 수 있으며, null인 항목은 변경되지 않습니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-24
 */
public record UserUpdateRequest(
        String currentPassword,

        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
        String nickname,

        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[\\x21-\\x7E]{8,20}$", message = "비밀번호는 영문, 숫자를 포함하여 8자 이상 20자 이하로 입력해주세요. (특수문자 사용 가능)")
        String password,

        @Min(value = 1, message = "프로필 이미지는 1~4 중 선택해주세요.")
        @Max(value = 4, message = "프로필 이미지는 1~4 중 선택해주세요.")
        Integer profileImage
) {
}
