package com.eof.back.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 로그인 요청 데이터를 전달하는 DTO입니다.
 *
 * <p>클라이언트로부터 전달받은 로그인 정보를 캡슐화하며,
 * Validation 어노테이션을 통해 기본적인 입력값 검증을 수행합니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
public record LoginRequest(
        @NotBlank(message = "아이디는 필수입니다.")
        @Size(max = 50, message = "아이디는 50자를 초과할 수 없습니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,

        @Size(max = 2048, message = "잘못된 보안 문자 토큰입니다.")
        String captchaToken
) {
}
