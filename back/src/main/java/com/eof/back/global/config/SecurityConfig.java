package com.eof.back.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 관련 Bean을 등록하는 설정 클래스입니다.
 *
 * <p>현재는 사용자 비밀번호를 안전하게 저장하기 위해
 *  * {@link PasswordEncoder} Bean을 등록합니다.</p>
 *
 * <p>BCrypt 알고리즘을 사용하는 {@link BCryptPasswordEncoder}를 통해
 * 비밀번호를 해시 형태로 암호화하여 데이터베이스에 저장할 수 있도록 합니다.</p>
 *
 * <p>회원가입 시 비밀번호 암호화 및 로그인 시 비밀번호 검증에 사용됩니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@Configuration
public class SecurityConfig {

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder Bean을 등록합니다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**",

                                "/api/v1/users/signup",
                                "/api/v1/users/login"
                        ).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
