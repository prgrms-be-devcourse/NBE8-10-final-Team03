package com.eof.back.domain.image.controller;

import com.eof.back.domain.image.dto.ImageUploadResponse;
import com.eof.back.domain.image.service.ImageService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 리소스와 관련된 HTTP 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 클라이언트로부터 이미지 파일을 전송받아 서비스 계층으로 전달하고,
 * 처리 결과를 표준 응답 포맷({@link Response})으로 반환합니다.
 * 업로드된 이미지 URL은 호출자가 원하는 도메인(Quiz, QuizSet 등)의 imageUrl 필드에 저장합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @RestController} 어노테이션을 통해 스프링 MVC 컨트롤러 빈으로 등록됩니다.
 *
 * <p><b>권한 모델:</b><br>
 * - <b>업로드:</b> 인증된 모든 사용자가 가능합니다.<br>
 * - <b>삭제:</b> 업로드한 본인 또는 ADMIN만 가능합니다.
 *
 * @author Minji-032
 * @since 2026-04-06
 */
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지를 업로드합니다.
     * <p>
     * <b>API:</b> [POST] /api/v1/images <br>
     * <b>Content-Type:</b> multipart/form-data
     * </p>
     *
     * @param file      업로드할 이미지 파일 (key: "file")
     * @param principal 현재 로그인한 사용자의 정보
     * @return 업로드된 이미지의 접근 URL을 담은 성공 응답
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<ImageUploadResponse>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {

        ImageUploadResponse response = imageService.uploadImage(file, principal.id());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 이미지를 삭제합니다.
     * <p>
     * <b>API:</b> [DELETE] /api/v1/images <br>
     * 업로드한 본인 또는 ADMIN만 삭제할 수 있습니다.
     * </p>
     *
     * @param imageUrl  삭제할 이미지의 접근 URL (query parameter)
     * @param principal 현재 로그인한 사용자의 정보
     * @return 삭제 성공 응답 (HTTP 204 No Content)
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteImage(
            @RequestParam String imageUrl,
            @AuthenticationPrincipal UserPrincipal principal) {

        imageService.deleteImage(imageUrl, principal.id());
        return ResponseEntity.noContent().build();
    }
}
