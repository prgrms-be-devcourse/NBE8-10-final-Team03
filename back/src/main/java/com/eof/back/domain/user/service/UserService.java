package com.eof.back.domain.user.service;


import com.eof.back.domain.user.dto.UserInfoResponse;
import com.eof.back.domain.user.dto.UserUpdateRequest;
import com.eof.back.domain.user.dto.UserUpdateResponse;

/**
 * 사용자 도메인과 관련된 기능의 계약을 정의하는 서비스 인터페이스입니다.
 *
 * <p>사용자 조회 등 사용자 관련 기능의 명세를 정의합니다.</p>
 *
 * <p><b>주요 기능:</b><br>
 * - 내 정보 조회
 *
 * @author 5h6vm
 * @since 2026-03-18
 */

public interface UserService {

    /**
     * 로그인한 사용자의 정보를 조회합니다.
     *
     * @param userId 로그인한 사용자 ID
     * @return 사용자 기본 정보
     */
    UserInfoResponse getInfo(Long userId);

    /**
     * 로그인한 사용자의 정보를 수정합니다.
     *
     * <p>닉네임과 비밀번호를 선택적으로 수정할 수 있으며, null인 항목은 변경되지 않습니다.</p>
     *
     * @param userId  로그인한 사용자 ID
     * @param request 수정할 정보 (닉네임, 비밀번호)
     * @return 수정된 사용자 기본 정보
     */
    UserUpdateResponse updateInfo(Long userId, UserUpdateRequest request);
}
