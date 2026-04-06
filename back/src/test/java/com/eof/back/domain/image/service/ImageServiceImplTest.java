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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

/**
 * ImageServiceImpl의 단위 테스트입니다.
 * <p>
 * uploadImage, deleteImage 메서드의 성공 및 실패 시나리오를 검증합니다.
 *
 * @author Minji-032
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ImageServiceImplTest {

    @Mock private ObjectStorage objectStorage;
    @Mock private ImageRepository imageRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ImageServiceImpl imageService;

    @Nested
    @DisplayName("uploadImage")
    class UploadImage {

        @Test
        @DisplayName("성공 - 유효한 파일을 업로드하면 accessUrl을 반환한다")
        void success() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[100]);
            User uploader = mock(User.class);
            given(userRepository.getReferenceById(1L)).willReturn(uploader);
            given(objectStorage.upload(any(), anyString())).willReturn("https://s3.../uuid.jpg");

            // when
            ImageUploadResponse result = imageService.uploadImage(file, 1L);

            // then
            assertThat(result.accessUrl()).isEqualTo("https://s3.../uuid.jpg");
            verify(imageRepository).saveAndFlush(any(Image.class));
        }

        @Test
        @DisplayName("실패 - 빈 파일이면 IMAGE_EMPTY 예외가 발생한다")
        void fail_emptyFile() {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

            assertThatThrownBy(() -> imageService.uploadImage(file, 1L))
                    .isInstanceOf(ImageException.class)
                    .satisfies(e -> assertThat(((ImageException) e).getErrorCode())
                            .isEqualTo(ImageErrorCode.IMAGE_EMPTY));
        }

        @Test
        @DisplayName("실패 - 허용되지 않는 확장자면 IMAGE_INVALID_EXTENSION 예외가 발생한다")
        void fail_invalidExtension() {
            MockMultipartFile file = new MockMultipartFile("file", "test.webp", "image/webp", new byte[100]);

            assertThatThrownBy(() -> imageService.uploadImage(file, 1L))
                    .isInstanceOf(ImageException.class)
                    .satisfies(e -> assertThat(((ImageException) e).getErrorCode())
                            .isEqualTo(ImageErrorCode.IMAGE_INVALID_EXTENSION));
        }

        @Test
        @DisplayName("실패 - DB 저장 실패 시 S3 파일을 롤백하고 IMAGE_UPLOAD_FAILED 예외가 발생한다")
        void fail_dbSaveFails_rollbackS3() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[100]);
            User uploader = mock(User.class);
            given(userRepository.getReferenceById(1L)).willReturn(uploader);
            given(objectStorage.upload(any(), anyString())).willReturn("https://s3.../uuid.jpg");
            willThrow(new RuntimeException("DB error")).given(imageRepository).saveAndFlush(any());

            // when & then
            assertThatThrownBy(() -> imageService.uploadImage(file, 1L))
                    .isInstanceOf(ImageException.class)
                    .satisfies(e -> assertThat(((ImageException) e).getErrorCode())
                            .isEqualTo(ImageErrorCode.IMAGE_UPLOAD_FAILED));

            verify(objectStorage).delete(anyString()); // S3 롤백 확인
        }
    }

    @Nested
    @DisplayName("deleteImage")
    class DeleteImage {

        @Test
        @DisplayName("성공 - 본인이 업로드한 이미지를 삭제한다")
        void success_owner() {
            // given
            User uploader = mock(User.class);
            given(uploader.getId()).willReturn(1L);

            Image image = mock(Image.class);
            given(image.getUploader()).willReturn(uploader);

            User requester = mock(User.class);
            given(requester.getRole()).willReturn(Role.USER);

            given(imageRepository.findByAccessUrl(anyString())).willReturn(Optional.of(image));
            given(userRepository.findById(1L)).willReturn(Optional.of(requester));
            given(objectStorage.parsePath(anyString())).willReturn("uuid.jpg");

            // when
            imageService.deleteImage("https://s3.../uuid.jpg", 1L);

            // then
            verify(objectStorage).delete("uuid.jpg");
            verify(imageRepository).delete(image);
        }

        @Test
        @DisplayName("성공 - ADMIN은 타인의 이미지도 삭제할 수 있다")
        void success_admin() {
            // given
            User uploader = mock(User.class);
            given(uploader.getId()).willReturn(2L);

            Image image = mock(Image.class);
            given(image.getUploader()).willReturn(uploader);

            User admin = mock(User.class);
            given(admin.getRole()).willReturn(Role.ADMIN);

            given(imageRepository.findByAccessUrl(anyString())).willReturn(Optional.of(image));
            given(userRepository.findById(1L)).willReturn(Optional.of(admin));
            given(objectStorage.parsePath(anyString())).willReturn("uuid.jpg");

            // when
            imageService.deleteImage("https://s3.../uuid.jpg", 1L);

            // then
            verify(objectStorage).delete("uuid.jpg");
            verify(imageRepository).delete(image);
        }

        @Test
        @DisplayName("성공 - imageUrl이 null이면 아무것도 하지 않는다")
        void skip_nullUrl() {
            imageService.deleteImage(null, 1L);
            verifyNoInteractions(imageRepository, objectStorage);
        }

        @Test
        @DisplayName("성공 - imageUrl이 공백이면 아무것도 하지 않는다")
        void skip_blankUrl() {
            imageService.deleteImage("  ", 1L);
            verifyNoInteractions(imageRepository, objectStorage);
        }

        @Test
        @DisplayName("실패 - DB에 이미지가 없으면 IMAGE_NOT_FOUND 예외가 발생한다")
        void fail_imageNotFound() {
            given(imageRepository.findByAccessUrl(anyString())).willReturn(Optional.empty());

            assertThatThrownBy(() -> imageService.deleteImage("https://s3.../uuid.jpg", 1L))
                    .isInstanceOf(ImageException.class)
                    .satisfies(e -> assertThat(((ImageException) e).getErrorCode())
                            .isEqualTo(ImageErrorCode.IMAGE_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 요청 사용자가 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다")
        void fail_userNotFound() {
            // given
            Image image = mock(Image.class);
            given(imageRepository.findByAccessUrl(anyString())).willReturn(Optional.of(image));
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> imageService.deleteImage("https://s3.../uuid.jpg", 1L))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 본인이 아니고 ADMIN도 아니면 USER_AUTH_FAIL 예외가 발생한다")
        void fail_unauthorized() {
            // given
            User uploader = mock(User.class);
            given(uploader.getId()).willReturn(2L);

            Image image = mock(Image.class);
            given(image.getUploader()).willReturn(uploader);

            User requester = mock(User.class);
            given(requester.getRole()).willReturn(Role.USER);

            given(imageRepository.findByAccessUrl(anyString())).willReturn(Optional.of(image));
            given(userRepository.findById(1L)).willReturn(Optional.of(requester));

            // when & then
            assertThatThrownBy(() -> imageService.deleteImage("https://s3.../uuid.jpg", 1L))
                    .isInstanceOf(AuthException.class)
                    .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                            .isEqualTo(AuthErrorCode.USER_AUTH_FAIL));
        }

        @Test
        @DisplayName("실패 - parsePath가 빈 문자열이면 IMAGE_DELETE_FAILED 예외가 발생한다")
        void fail_parsePathBlank() {
            // given
            User uploader = mock(User.class);
            given(uploader.getId()).willReturn(1L);

            Image image = mock(Image.class);
            given(image.getUploader()).willReturn(uploader);

            User requester = mock(User.class);
            given(requester.getRole()).willReturn(Role.USER);

            given(imageRepository.findByAccessUrl(anyString())).willReturn(Optional.of(image));
            given(userRepository.findById(1L)).willReturn(Optional.of(requester));
            given(objectStorage.parsePath(anyString())).willReturn("");

            // when & then
            assertThatThrownBy(() -> imageService.deleteImage("https://invalid-url", 1L))
                    .isInstanceOf(ImageException.class)
                    .satisfies(e -> assertThat(((ImageException) e).getErrorCode())
                            .isEqualTo(ImageErrorCode.IMAGE_DELETE_FAILED));
        }
    }
}
