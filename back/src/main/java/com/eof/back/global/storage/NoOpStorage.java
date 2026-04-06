package com.eof.back.global.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * AWS S3가 비활성화되었을 때 사용하는 가짜 저장소 구현체입니다.
 * <p>
 * {@code custom.s3.enabled=false} 이거나 해당 프로퍼티가 없을 때 활성화됩니다.
 * 로컬 개발 환경에서 S3 연동 없이 이미지 업로드/삭제 흐름을 테스트할 수 있습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ObjectStorage}의 구현 클래스입니다.
 *
 * @author Minji-032
 * @see ObjectStorage
 * @since 2026-04-06
 */
@Component
@ConditionalOnProperty(prefix = "custom.s3", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpStorage implements ObjectStorage {

    @Override
    public String upload(MultipartFile file, String destination) {
        return "http://localhost:8080/temp-url/" + destination; // 실제 업로드는 하지 않음
    }

    @Override
    public void delete(String destination) { /* 아무것도 하지 않음 */ }

    @Override
    public String parsePath(String url) { return ""; }
}
