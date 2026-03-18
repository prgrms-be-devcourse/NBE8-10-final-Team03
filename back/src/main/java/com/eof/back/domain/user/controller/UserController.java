package com.eof.back.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 관련 API 요청을 처리하는 컨트롤러입니다.
 *
 * <p>회원가입, 로그인, 사용자 정보 조회 등
 * 사용자 도메인과 관련된 HTTP 요청을 처리합니다.</p>
 *
 * <p>현재는 사용자 API의 기본 구조만 정의하며,
 * 이후 회원가입 및 인증 관련 기능을 순차적으로 추가할 예정입니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
}
