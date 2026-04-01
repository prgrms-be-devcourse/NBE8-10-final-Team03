package com.eof.back.global.jwt;

import com.eof.back.global.exception.exceptions.AuthException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 기반 사용자 인증을 처리하는 필터입니다.
 * <p>
 * 모든 HTTP 요청에 대해 쿠키의 accessToken을 확인하고,
 * 토큰이 유효한 경우 사용자 정보를 생성하여 SecurityContext에 저장합니다.
 * 이를 통해 이후의 인가 처리에서 로그인된 사용자로 식별할 수 있도록 합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link OncePerRequestFilter}를 상속하여 하나의 요청당 한 번만 실행되는 필터로 동작합니다.
 *
 * <p><b>빈 관리:</b><br>
 * SecurityConfig에서 필터 체인에 등록하여 사용합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Security의 SecurityContextHolder, UsernamePasswordAuthenticationToken을 사용하여
 * 인증 객체를 생성하고 보안 컨텍스트에 저장합니다.
 *
 * @author 5h6vm
 * @see JwtTokenProvider
 * @since 2026-03-19
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    /**
     * 요청마다 JWT 인증을 수행합니다.
     *
     * <p>쿠키에서 accessToken을 추출한 뒤,
     * 토큰이 유효하면 사용자 정보와 권한을 기반으로 인증 객체를 생성하여
     * SecurityContext에 저장합니다.
     *
     * <p>토큰이 없거나 유효하지 않은 경우 인증 정보를 저장하지 않고 다음 필터로 전달합니다.
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 다음 필터 체인
     * @throws ServletException 서블릿 처리 중 예외가 발생한 경우
     * @throws IOException      I/O 예외가 발생한 경우
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 쿠키에서 accessToken 추출
        String token = cookieUtil.resolveToken(request, CookieUtil.ACCESS_TOKEN_COOKIE).orElse(null);

        try {
            // 2. 토큰이 존재하면 검증 후 Claims 반환 (파싱 1회)
            if (token != null) {
                Claims claims = jwtTokenProvider.validateToken(token);

                // 3. Claims에서 사용자 정보 추출
                Long userId = jwtTokenProvider.getUserId(claims);
                String username = jwtTokenProvider.getUsername(claims);
                String role = jwtTokenProvider.getRole(claims);
                String nickname = jwtTokenProvider.getNickname(claims);

                // 4. 권한 생성
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role));

                // 5. 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                new UserPrincipal(userId, username, nickname, role),
                                null,
                                authorities
                        );

                // 6. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (AuthException e) {
            // 인증 실패 시 context 비움
            SecurityContextHolder.clearContext();
        }

        // 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }

}
