package com.eof.back.domain.image.service;

import com.eof.back.domain.image.dto.ImageUploadResponse;
import com.eof.back.domain.image.entity.Image;
import com.eof.back.domain.image.repository.ImageRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.ImageErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.ImageException;
import com.eof.back.global.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * 이미지 업로드 및 메타데이터 관리를 담당하는 서비스 구현체입니다.
 * <p>
 * {@link ImageService} 인터페이스를 구현하여 실제 비즈니스 로직을 수행합니다.
 * {@link ObjectStorage}를 통해 물리적 파일을 저장하고, {@link ImageRepository}를 통해 DB에 메타데이터를 저장합니다.
 * 반환된 URL은 호출자(Quiz, QuizSet 등)가 각자의 imageUrl 필드에 저장합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ImageService} 인터페이스를 구현합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service} 어노테이션을 통해 스프링 빈으로 등록됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * {@code ObjectStorage}: 추상화된 파일 저장소 인터페이스를 사용합니다. <br>
 * {@code ImageRepository}: JPA를 통해 DB와 통신합니다.
 *
 * @author Minji-032
 * @since 2026-04-06
 */
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif");

    private final ObjectStorage objectStorage;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ImageUploadResponse uploadImage(MultipartFile file, Long userId) {
        validateFile(file);

        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(
                        AuthErrorCode.USER_NOT_FOUND,
                        "[ImageServiceImpl#uploadImage] user not found. userId=" + userId,
                        "사용자를 찾을 수 없습니다."
                ));

        String originalName = file.getOriginalFilename();
        String storedName = createStoredName(originalName);

        String accessUrl = objectStorage.upload(file, storedName);

        Image image = Image.builder()
                .originalName(originalName)
                .storedName(storedName)
                .accessUrl(accessUrl)
                .uploader(uploader)
                .build();

        try {
            imageRepository.saveAndFlush(image);
        } catch (Exception e) {
            objectStorage.delete(storedName); // DB 저장 실패 시 S3 롤백
            throw new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAILED,
                    "[ImageServiceImpl#uploadImage] DB save failed. storedName=" + storedName + ", cause=" + e.getMessage(),
                    "이미지 업로드 중 오류가 발생했습니다.");
        }

        return new ImageUploadResponse(accessUrl);
    }

    @Override
    @Transactional
    public void deleteImage(String imageUrl, Long userId) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        Image image = imageRepository.findByAccessUrlWithUploader(imageUrl)
                .orElseThrow(() -> new ImageException(
                        ImageErrorCode.IMAGE_NOT_FOUND,
                        "[ImageServiceImpl#deleteImage] image not found in DB. url=" + imageUrl,
                        "해당 이미지를 찾을 수 없습니다."
                ));

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(
                        AuthErrorCode.USER_NOT_FOUND,
                        "[ImageServiceImpl#deleteImage] user not found. userId=" + userId,
                        "사용자를 찾을 수 없습니다."
                ));

        boolean isOwner = image.getUploader().getId().equals(userId);
        boolean isAdmin = requester.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AuthException(
                    AuthErrorCode.USER_AUTH_FAIL,
                    "[ImageServiceImpl#deleteImage] unauthorized deletion. uploaderId=" + image.getUploader().getId() + ", requesterId=" + userId,
                    "이미지를 삭제할 권한이 없습니다."
            );
        }

        String key = objectStorage.parsePath(imageUrl);
        if (key.isBlank()) {
            throw new ImageException(
                    ImageErrorCode.IMAGE_DELETE_FAILED,
                    "[ImageServiceImpl#deleteImage] failed to parse image path. url=" + imageUrl,
                    "이미지 삭제 중 오류가 발생했습니다."
            );
        }
        imageRepository.delete(image);
        objectStorage.delete(key);
    }

    /**
     * 파일 유효성을 검증합니다.
     * 빈 파일, 허용되지 않는 확장자를 검사합니다.
     *
     * @param file 검증할 파일
     * @throws ImageException 유효성 검증 실패 시 발생합니다.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new ImageException(
                    ImageErrorCode.IMAGE_EMPTY,
                    "[ImageServiceImpl#validateFile] file is empty or filename is null",
                    "이미지 파일이 비어있거나 잘못된 요청입니다."
            );
        }
        if (!isValidExtension(file.getOriginalFilename())) {
            throw new ImageException(
                    ImageErrorCode.IMAGE_INVALID_EXTENSION,
                    "[ImageServiceImpl#validateFile] invalid extension. filename=" + file.getOriginalFilename(),
                    "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 가능)"
            );
        }
    }

    /**
     * 허용된 확장자 여부를 확인합니다.
     *
     * @param filename 원본 파일명
     * @return 허용된 확장자면 true
     */
    private boolean isValidExtension(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }

    /**
     * UUID 기반의 S3 저장 경로(Object Key)를 생성합니다.
     *
     * @param originalName 원본 파일명
     * @return {uuid}.{ext} 형태의 저장 경로
     */
    private String createStoredName(String originalName) {
        String ext = originalName.substring(originalName.lastIndexOf("."));
        return UUID.randomUUID() + ext;
    }
}
