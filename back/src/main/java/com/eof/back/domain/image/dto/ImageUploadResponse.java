package com.eof.back.domain.image.dto;

/**
 * 이미지 업로드 완료 후 클라이언트에게 반환되는 응답 DTO입니다.
 * <p>
 * 업로드된 이미지의 접근 URL을 반환합니다.
 * 호출자는 반환된 URL을 원하는 도메인(Quiz, QuizSet 등)의 imageUrl 필드에 저장합니다.
 *
 * @param accessUrl 업로드된 이미지의 S3 접근 URL
 * @author Minji-032
 * @since 2026-04-06
 */
public record ImageUploadResponse(String accessUrl) {
}
