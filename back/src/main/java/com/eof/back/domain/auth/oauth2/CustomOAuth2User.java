package com.eof.back.domain.auth.oauth2;

import com.eof.back.domain.user.user.entity.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 소셜 로그인 인증 완료 후 Spring Security 컨텍스트에서 사용할 커스텀 OAuth2User 구현체입니다.
 *
 * <p>{@link CustomOAuth2UserService}에서 DB User를 조회/생성한 뒤 이 객체로 감싸 반환합니다.
 * {@link OAuth2SuccessHandler}는 이 객체에서 바로 사용자 정보를 꺼내 JWT를 발급하므로,
 * DB를 재조회하거나 OAuthAttributes를 재파싱할 필요가 없습니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final Long userId;
    private final String username;
    private final Role role;
    private final String nickname;
    private final boolean active;
    private final Integer profileImage;

    @Override
    public Map<String, Object> getAttributes() { return delegate.getAttributes(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getName() { return username; }
}
