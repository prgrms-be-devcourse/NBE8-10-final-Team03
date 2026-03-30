package com.eof.back.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(springdoc-openapi) 설정 클래스입니다.
 *
 * <p>쿠키 기반 인증을 사용하므로 Bearer 토큰 SecurityScheme은 사용하지 않습니다.
 * Swagger UI에서 로그인 엔드포인트를 호출하면 쿠키가 자동으로 설정되며,
 * 이후 요청에 쿠키가 자동으로 포함됩니다. (withCredentials: true 설정 필요)
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NBE8-10 Team03 API")
                        .version("v1")
                );
    }
}
