package com.eof.back.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(springdoc-openapi) 설정 클래스입니다.
 *
 * <p>JWT Bearer 토큰 인증을 Swagger UI에서 사용할 수 있도록 SecurityScheme을 등록합니다.
 * 우측 상단 Authorize 버튼을 통해 토큰을 입력하면 이후 모든 요청에 자동으로 포함됩니다.
 *
 * @author 5h6vm
 * @since 2026-03-23
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NBE8-10 Team03 API")
                        .version("v1")
                )
                // 모든 API 엔드포인트에 BearerAuth 인증을 전역 적용
                // → 각 API마다 자물쇠 아이콘이 표시됨
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)  // HTTP 표준 인증 방식
                                .scheme("bearer")                // Authorization: Bearer <token> 형식
                                .bearerFormat("JWT")             // UI 표시용 힌트 (실제 동작에 영향 없음)
                        )
                );
    }
}
