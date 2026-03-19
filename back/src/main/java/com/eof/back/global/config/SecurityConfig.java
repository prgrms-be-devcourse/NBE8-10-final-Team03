package com.eof.back.global.config;

import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtAuthenticationFilter;
import com.eof.back.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 전반 설정을 담당하는 클래스입니다.
 *
 * <p>JWT 기반 인증을 사용하는 Stateless 구조로 설정하며,
 * 다음과 같은 보안 정책을 구성합니다.</p>
 *
 * <p><b>주요 기능:</b><br>
 * - PasswordEncoder Bean 등록 (BCrypt)
 * - JWT 인증 필터 등록
 * - 인증/인가 정책 설정 (permitAll / authenticated)
 * - 세션 미사용(Stateless) 설정
 *
 * <p><b>보안 흐름:</b><br>
 * 1. 요청 발생<br>
 * 2. JwtAuthenticationFilter에서 토큰 검증<br>
 * 3. 유효한 경우 SecurityContext에 인증 저장<br>
 * 4. 이후 인가 처리 진행
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder Bean을 등록합니다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인을 구성합니다.
     *
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 중 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**",

                                "/api/v1/auth/signup",
                                "/api/v1/auth/login"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }
}
