package com.eof.back.global.jwt;

/**
 * Spring Security의 인증 주체(Principal)를 나타내는 클래스입니다.
 *
 * <p>JWT 인증 필터에서 SecurityContext에 저장되며,
 * 컨트롤러에서 {@link org.springframework.security.core.annotation.AuthenticationPrincipal}을 통해 접근합니다.
 *
 * @param id       사용자 ID
 * @param username 사용자 아이디
 * @param nickname 사용자 닉네임
 * @param role     사용자 권한
 * @author 5h6vm
 * @since 2026-03-23
 */
public record UserPrincipal(
        Long id,
        String username,
        String nickname,
        String role
) {
}
