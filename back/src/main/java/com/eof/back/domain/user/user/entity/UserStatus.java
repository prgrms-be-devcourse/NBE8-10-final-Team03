package com.eof.back.domain.user.user.entity;

/**
 * 사용자 계정의 상태를 정의하는 열거형입니다.
 *
 * <ul>
 *     <li>ACTIVE : 정상 활동 중인 사용자</li>
 *     <li>DELETED : 탈퇴 처리된 사용자</li>
 * </ul>
 *
 * @author 5h6vm
 * @since 2026-03-27
 */
public enum UserStatus {
    ACTIVE,
    SUSPENDED,
    DELETED
}
