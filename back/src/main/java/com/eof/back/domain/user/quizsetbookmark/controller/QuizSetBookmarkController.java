package com.eof.back.domain.user.quizsetbookmark.controller;

import com.eof.back.domain.user.quizsetbookmark.dto.BookmarkCreateResponse;
import com.eof.back.domain.user.quizsetbookmark.service.QuizSetBookmarkService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author 5h6vm
 * @see
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

    @DeleteMapping("/{quizSetId}/bookmark")
    public ResponseEntity<Response<Void>> deleteBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long quizSetId
    ) {
        quizSetBookmarkService.deleteBookmark(userPrincipal.id(), quizSetId);

        return ResponseEntity.ok(
                CommonResponse.success(null, "퀴즈셋 북마크가 제거되었습니다.")
        );
    }
}
