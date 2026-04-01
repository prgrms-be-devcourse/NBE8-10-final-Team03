package com.eof.back.domain.auth.dto;

/**
 * 로그인 처리 결과를 서비스에서 컨트롤러로 전달하기 위한 내부 DTO입니다.
 *
 * <p>클라이언트에 직접 반환되지 않으며, 컨트롤러 내부에서만 사용됩니다.</p>
 *
 * <p>주요 목적:</p>
 * <ul>
 *     <li>발급된 AccessToken, RefreshToken을 컨트롤러로 전달 (쿠키 설정에 사용)</li>
 *     <li>클라이언트 응답용 LoginResponse 생성에 필요한 사용자 정보 전달</li>
 * </ul>
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
public record LoginResult(
        String accessToken,
        String refreshToken,
        Long userId,
        String nickname,
        String role
) {
}
