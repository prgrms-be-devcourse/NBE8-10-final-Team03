package com.eof.back.domain.image.service;

import com.eof.back.domain.image.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 업로드 및 삭제에 대한 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 물리적 파일 저장은 {@link com.eof.back.global.storage.ObjectStorage ObjectStorage}에 위임하고,
 * 메타데이터는 DB에 저장합니다.
 * 업로드한 본인 또는 ADMIN만 삭제할 수 있습니다.
 * </p>
 *
 * @author Minji-032
 * @since 2026-04-06
 */
public interface ImageService {

    /**
     * 이미지를 스토리지에 업로드하고 메타데이터를 DB에 저장합니다.
     * <p>
     * 파일 유효성 검증(빈 파일, 확장자, 용량)을 수행한 후 업로드합니다.
     * 반환된 URL은 호출자가 원하는 도메인(Quiz, QuizSet 등)의 imageUrl 필드에 저장합니다.
     * </p>
     *
     * @param file   업로드할 이미지 파일
     * @param userId 요청을 보낸 사용자의 식별자
     * @return 업로드된 이미지의 접근 URL
     * @throws com.eof.back.global.exception.exceptions.ImageException 파일 검증 실패 또는 업로드 실패 시 발생합니다.
     */
    ImageUploadResponse uploadImage(MultipartFile file, Long userId);

    /**
     * 이미지를 스토리지와 DB에서 삭제합니다.
     * <p>
     * 업로드한 본인 또는 ADMIN만 삭제할 수 있습니다.
     * </p>
     *
     * @param imageUrl 삭제할 이미지의 접근 URL
     * @param userId   요청을 보낸 사용자의 식별자
     * @throws com.eof.back.global.exception.exceptions.ImageException   해당 이미지를 찾을 수 없을 경우 발생합니다.
     * @throws com.eof.back.global.exception.exceptions.AuthException    삭제 권한이 없을 경우 발생합니다.
     */
    void deleteImage(String imageUrl, Long userId);
}
