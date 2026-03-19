package com.eof.back.domain.user.service;


import com.eof.back.domain.user.dto.UserSignupRequest;
import com.eof.back.domain.user.dto.UserSignupResponse;

/**
 * 사용자 도메인과 관련된 기능의 계약을 정의하는 서비스 인터페이스입니다.
 *
 * <p>회원가입, 로그인, 사용자 조회 등 사용자 관련 기능의 명세를 정의합니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-18
 */

public interface UserService {

    /**
     * 회원가입을 처리합니다.
     *
     * @param req 회원가입 요청 데이터
     * @return 회원가입 결과 데이터
     */
    UserSignupResponse signup(UserSignupRequest req);

}
