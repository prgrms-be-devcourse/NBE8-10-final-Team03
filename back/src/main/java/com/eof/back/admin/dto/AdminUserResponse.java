package com.eof.back.admin.dto;

import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.entity.UserStatus;
import java.time.LocalDateTime;

/**
 * 관리자 페이지에서 사용자 정보를 조회할 때 사용하는 응답 DTO입니다.
 *
 * @param id                사용자 식별자
 * @param username          사용자 아이디
 * @param nickname          사용자 닉네임
 * @param email             사용자 이메일
 * @param role              사용자 권한
 * @param status            사용자 상태
 * @param totalRankingScore 누적 랭킹 점수
 * @param createdAt         가입 일시
 */
public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        Role role,
        UserStatus status,
        Long totalRankingScore,
        LocalDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getTotalRankingScore(),
                user.getCreatedAt()
        );
    }
}
