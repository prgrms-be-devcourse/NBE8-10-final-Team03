package com.eof.back.domain.user.user.entity;

/**
 * 사용자 인증 제공자를 구분하는 열거형입니다.
 *
 * <p>일반 회원가입 사용자와 소셜 로그인 사용자를 구별하며,
 * 소셜 로그인 시 어떤 제공자를 통해 인증했는지를 나타냅니다.</p>
 *
 * <p>주요 목적:</p>
 * <ul>
 *     <li>LOCAL — 자체 회원가입 (username/password 방식)</li>
 *     <li>GOOGLE — Google OAuth2 소셜 로그인</li>
 *     <li>KAKAO — Kakao OAuth2 소셜 로그인</li>
 * </ul>
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE,
    KAKAO
}
