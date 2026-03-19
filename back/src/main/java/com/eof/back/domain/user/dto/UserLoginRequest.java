package com.eof.back.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 데이터를 전달하는 DTO입니다.
 *
 * <p>클라이언트로부터 전달받은 로그인 정보를 캡슐화하며,
 * Validation 어노테이션을 통해 기본적인 입력값 검증을 수행합니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-19
 */
public record UserLoginRequest(
        @NotBlank(message = "아이디는 필수입니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
