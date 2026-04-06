package com.eof.back.global.storage;

import com.eof.back.global.exception.errorCode.ImageErrorCode;
import com.eof.back.global.exception.exceptions.ImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

/**
 * AWS S3 객체 스토리지와의 통신을 담당하는 구현체 클래스입니다.
 * <p>
 * {@link ObjectStorage} 인터페이스를 구현하여 파일 업로드, 삭제, 경로 파싱 등의 기능을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ObjectStorage} 인터페이스를 구현합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code S3Storage(S3Client s3Client)} <br>
 * S3 클라이언트 객체를 주입받아 초기화합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Component} 어노테이션을 통해 스프링 빈으로 등록됩니다. <br>
 * {@code @ConditionalOnProperty(name = "enabled", havingValue = "true")} 설정에 의해
 * application.yml의 custom.s3.enabled 속성이 true일 경우에만 빈이 생성됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * {@code software.amazon.awssdk:s3} 라이브러리를 사용하여 AWS S3와 통신합니다.
 *
 * @author Minji-032
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "custom.s3", name = "enabled", havingValue = "true")
public class S3Storage implements ObjectStorage {

    private final S3Client s3Client;

    @Value("${custom.s3.bucket}")
    private String bucket;

    @Value("${custom.s3.region}")
    private String region;

    @Override
    public String upload(MultipartFile file, String destination) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(destination)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + destination;

        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAILED,
                    "[S3Storage#upload] failed. dest=" + destination + ", cause=" + e.getMessage(),
                    "이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void delete(String destination) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(destination)
                            .build()
            );
        } catch (Exception e) {
            throw new ImageException(ImageErrorCode.IMAGE_DELETE_FAILED,
                    "[S3Storage#delete] failed. dest=" + destination + ", cause=" + e.getMessage(),
                    "이미지 삭제 중 오류가 발생했습니다.");
        }
    }

    @Override
    public String parsePath(String url) {
        String baseUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
        if (url == null || !url.startsWith(baseUrl)) {
            return "";
        }
        return url.substring(baseUrl.length());
    }
}
