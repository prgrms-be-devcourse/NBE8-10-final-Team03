package com.eof.back.global.jwt;

import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.token.TokenVersionStore;
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
    private final TokenVersionStore tokenVersionStore;
    private final CookieUtil cookieUtil;

    /**
     * 요청마다 JWT 인증 및 tokenVersion 검증을 수행합니다.
     *
     * <p>쿠키에서 accessToken을 추출한 뒤 아래 순서로 검증합니다:
     * <ol>
     *   <li>JWT 서명·만료 검증 (위변조 및 만료 토큰 차단)</li>
     *   <li>tokenVersion 검증 - JWT의 version과 Redis 저장 version을 비교합니다.
     *       <ul>
     *         <li>Redis에 키 없음: 로그아웃·삭제 상태 → 인증 실패</li>
     *         <li>version 불일치: 다른 기기 로그인 또는 관리자 강제 무효화 → 인증 실패</li>
     *       </ul>
     *   </li>
     *   <li>검증 통과 시 SecurityContext에 인증 객체 저장</li>
     * </ol>
     *
     * <p>토큰이 없거나 검증에 실패한 경우 인증 정보를 저장하지 않고 다음 필터로 전달합니다.
     * 이후 인가 처리에서 {@code 401 Unauthorized}가 발생합니다.
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
                long tokenVersion = jwtTokenProvider.getTokenVersion(claims);

                // 4. tokenVersion 검증
                // Redis에 키가 없으면 로그아웃/탈퇴/관리자 삭제 상태 → 인증 실패
                // version 불일치면 다른 기기에서 재로그인하거나 관리자가 강제 무효화한 상태 → 인증 실패
                long storedVersion = tokenVersionStore.findByUserId(userId)
                        .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_INVALID));
                if (tokenVersion != storedVersion) {
                    throw new AuthException(AuthErrorCode.TOKEN_INVALID);
                }

                // 5. 권한 생성
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role));

                // 6. 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                new UserPrincipal(userId, username, nickname, role),
                                null,
                                authorities
                        );

                // 7. SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (AuthException e) {
            // JWT 검증 실패 또는 tokenVersion 불일치 시 context를 비워
            // 이후 인가 처리에서 401이 반환되도록 합니다.
            SecurityContextHolder.clearContext();
        }

        // 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }

}
