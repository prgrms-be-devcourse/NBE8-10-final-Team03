package com.eof.back.domain.user.dto;

/**
 *  회원가입 결과 응답 데이터를 전달하는 DTO입니다.
 *  *
 *  * <p>회원가입이 성공적으로 완료된 후 클라이언트에게 반환되는 정보를 담습니다.</p>
 *  *
 *  * <p>주요 목적:</p>
 *  * <ul>
 *  *     <li>회원가입 성공 여부에 따른 사용자 기본 정보 반환</li>
 *  *     <li>클라이언트에서 생성된 사용자 정보를 확인할 수 있도록 지원</li>
 *  * </ul>
 *  *
 *  * @author 5h6vm
 *  * @since 2026-03-18
 */
public record UserSignupResponse(
        Long userId,
        String username,
        String nickname
) {
}
