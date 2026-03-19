package com.eof.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>애플리케이션의 메인 진입점입니다.</p>
 * JPA Auditing을 활성화하여 BaseEntity의 생성/수정일 자동 관리를 지원합니다.
 *
 * @author MintyU
 * @since 2026-03-17
 */
@SpringBootApplication
public class BackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}

}
