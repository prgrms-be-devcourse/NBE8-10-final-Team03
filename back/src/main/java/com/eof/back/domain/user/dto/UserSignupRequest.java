package com.eof.back.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 데이터를 전달하는 DTO입니다.
 * <p>클라이언트로부터 전달받은 회원가입 정보를 캡슐화하며,
 *  * Validation 어노테이션을 통해 기본적인 입력값 검증을 수행합니다.</p>
 *
 * <p>검증 규칙:</p>
 * <ul>
 *     <li>username : 4자 이상 20자 이하, 영문/숫자만 사용</li>
 *     <li>password : 8자 이상 20자 이하, 영문/숫자 포함</li>
 *     <li>nickname : 2자 이상 20자 이하</li>
 * </ul>
 *
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
public record UserSignupRequest(
        @NotBlank(message = "아이디는 필수입니다.")
        @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해주세요.")
        @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$", message = "비밀번호는 영문, 숫자를 포함하여 8자 이상 20자 이하로 입력해주세요.")
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
        String nickname

) {
}
