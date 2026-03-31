package com.eof.back.global.config;

import com.eof.back.domain.auth.oauth2.CustomOAuth2UserService;
import com.eof.back.domain.auth.oauth2.OAuth2FailureHandler;
import com.eof.back.domain.auth.oauth2.OAuth2SuccessHandler;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtAuthenticationFilter;
import com.eof.back.global.security.SecurityUrlRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 전반 설정을 담당하는 클래스입니다.
 *
 * <p>JWT 기반 인증(HttpOnly 쿠키)과 OAuth2 소셜 로그인을 함께 지원하는 보안 설정 클래스입니다.
 * {@link SecurityUrlRegistry}에 정의된 URL 목록을 바탕으로 인가 정책을 구성합니다.</p>
 *
 * <p><b>주요 기능:</b><br>
 * - PasswordEncoder Bean 등록 (BCrypt)<br>
 * - JWT 인증 필터 등록 (쿠키에서 accessToken 읽기)<br>
 * - OAuth2 소셜 로그인 설정 (Google, Kakao)<br>
 * - CORS 설정 (Next.js localhost:3000 허용)<br>
 * - 인증/인가 정책 설정 (permitAll / authenticated)
 *
 * <p><b>세션 정책:</b><br>
 * OAuth2 인증 흐름에서 state 파라미터 보관을 위해 {@code IF_REQUIRED} 정책을 사용합니다.
 * 인증 완료 후 JWT 쿠키가 발급되므로, 일반 API 요청에서는 세션이 생성되지 않습니다.
 *
 * <p><b>보안 흐름:</b><br>
 * 1. 요청 발생<br>
 * 2. JwtAuthenticationFilter에서 쿠키의 accessToken 검증<br>
 * 3. 유효한 경우 SecurityContext에 인증 저장<br>
 * 4. 이후 인가 처리 진행
 *
 * @author 5h6vm
 * @since 2026-03-18
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    /**
     * JwtAuthenticationFilter의 서블릿 자동 등록을 비활성화합니다.
     *
     * <p>@Component가 붙은 OncePerRequestFilter는 Spring Boot에 의해 서블릿 필터로
     * 자동 등록됩니다. Security 필터 체인에도 등록되어 있으므로, 이중 실행을 방지합니다.
     *
     * @param filter JwtAuthenticationFilter 빈
     * @return 자동 등록이 비활성화된 FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * CORS 설정을 구성합니다.
     * <p>
     * Next.js 개발 서버(localhost:3000)의 요청을 허용하며,
     * Location 헤더를 노출하여 프론트엔드에서 리다이렉트 URL을 읽을 수 있도록 합니다.
     * 배포 시 allowedOriginPatterns에 실제 도메인을 추가해야 합니다.
     *
     * @return CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setExposedHeaders(List.of("Location"));
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Spring Security 필터 체인을 구성합니다.
     *
     * <p>JWT 쿠키 인증과 OAuth2 소셜 로그인을 함께 설정합니다.<br>
     * OAuth2 인증 흐름은 {@link CustomOAuth2UserService}에서 유저를 조회/생성하고,
     * {@link OAuth2SuccessHandler}에서 JWT 쿠키를 발급하여 프론트엔드로 redirect합니다.</p>
     *
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 중 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // OAuth2 인증 흐름 중 state 파라미터 보관을 위해 IF_REQUIRED 사용
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // SecurityContext를 세션이 아닌 요청 속성에만 저장
                // → JWT API 요청에서 의도치 않은 세션 생성 방지
                .securityContext(ctx -> ctx
                        .securityContextRepository(new RequestAttributeSecurityContextRepository())
                )
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(SecurityUrlRegistry.PERMIT_ALL_URLS).permitAll();
                    SecurityUrlRegistry.PUBLIC_METHOD_URLS.forEach((method, urls) ->
                            auth.requestMatchers(method, urls).permitAll()
                    );
                    auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
                    auth.anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                // 소셜 제공자에서 받은 사용자 정보로 DB 유저를 조회/생성
                                .userService(customOAuth2UserService)
                        )
                        // 인증 성공: JWT 쿠키 발급 후 프론트엔드로 redirect
                        .successHandler(oAuth2SuccessHandler)
                        // 인증 실패: error 파라미터와 함께 프론트엔드로 redirect
                        .failureHandler(oAuth2FailureHandler)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }
}