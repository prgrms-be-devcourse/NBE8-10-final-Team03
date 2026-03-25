package com.eof.back.domain.user.quizsetbookmark.controller;

import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkCreateResponse;
import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkItemResponse;
import com.eof.back.domain.user.quizsetbookmark.service.QuizSetBookmarkService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 퀴즈셋 북마크 관련 API 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 북마크 생성, 제거, 조회 엔드포인트를 제공하며, 모든 요청은 인증된 사용자만 접근할 수 있습니다.
 *
 * <p><b>기본 경로:</b> {@code /api/v1/users/me/bookmarks}
 *
 * <p><b>엔드포인트:</b>
 * <ul>
 *   <li>{@code POST /} - 퀴즈셋 북마크 추가</li>
 *   <li>{@code DELETE /{quizSetId}} - 퀴즈셋 북마크 제거</li>
 *   <li>{@code GET /} - 내 북마크 목록 조회</li>
 * </ul>
 *
 * @author 5h6vm
 * @see QuizSetBookmarkService
 * @since 2026-03-24
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/bookmarks")
public class QuizSetBookmarkController {

    private final QuizSetBookmarkService quizSetBookmarkService;

    @PostMapping
    public ResponseEntity<Response<BookmarkCreateResponse>> createBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long quizSetId
    ) {
        BookmarkCreateResponse response = quizSetBookmarkService.createBookmark(userPrincipal.id(), quizSetId);

        return ResponseEntity.ok(CommonResponse.success(response, "퀴즈셋 북마크가 추가되었습니다."));
    }

    @DeleteMapping("/{quizSetId}")
    public ResponseEntity<Response<Void>> deleteBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long quizSetId
    ) {
        quizSetBookmarkService.deleteBookmark(userPrincipal.id(), quizSetId);

        return ResponseEntity.ok(
                CommonResponse.success(null, "퀴즈셋 북마크가 제거되었습니다.")
        );
    }

    @GetMapping
    public ResponseEntity<Response<List<BookmarkItemResponse>>> getMyBookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<BookmarkItemResponse> response = quizSetBookmarkService.getBookmarks(userPrincipal.id());

        return ResponseEntity.ok(
                CommonResponse.success(response, "내 퀴즈셋 북마크 목록을 조회했습니다.")
        );
    }
}
