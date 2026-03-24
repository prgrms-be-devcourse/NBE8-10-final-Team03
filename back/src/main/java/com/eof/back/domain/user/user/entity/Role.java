package com.eof.back.domain.user.user.entity;

/**
 사용자 권한을 정의하는 열거형입니다.
 *
 * <p>Spring Security 인증 및 인가 과정에서 사용자의 접근 권한을 구분하기 위해 사용됩니다.</p>
 *
 * <ul>
 *     <li>USER : 일반 사용자</li>
 *     <li>ADMIN : 관리자</li>
 * </ul>
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
public enum Role {
    USER,
    ADMIN
}
