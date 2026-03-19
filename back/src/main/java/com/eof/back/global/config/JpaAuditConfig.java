package com.eof.back.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정을 위한 설정 클래스입니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
