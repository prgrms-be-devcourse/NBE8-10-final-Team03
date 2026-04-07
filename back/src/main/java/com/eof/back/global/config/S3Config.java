package com.eof.back.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 객체 스토리지 연동을 위한 설정 클래스입니다.
 * <p>
 * 애플리케이션 실행 시 S3 연결 정보를 로드하고,
 * {@link S3Client} 빈을 생성하여 스프링 컨테이너에 등록합니다.
 * 등록된 클라이언트는 이미지 파일 업로드 및 삭제 등의 작업에 사용됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * implementation("software.amazon.awssdk:s3:2.31.19")
 *
 * @author Minji-032
 * @since 2026-04-06
 */
@Configuration
@ConditionalOnProperty(prefix = "custom.s3", name = "enabled", havingValue = "true")
public class S3Config {

    @Value("${custom.s3.access-key}")
    private String accessKey;

    @Value("${custom.s3.secret-key}")
    private String secretKey;

    @Value("${custom.s3.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }
}
