package com.eof.back.domain.ai.controller;

import com.eof.back.domain.ai.dto.AiQuizGenerateResponse;
import com.eof.back.domain.ai.dto.AiQuizResponse;
import com.eof.back.domain.ai.service.AiQuizService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * AI 퀴즈 생성 API를 처리하는 REST 컨트롤러입니다.
 * <p>
 * {@code /api/v1/ai} 경로로 들어오는 요청을 처리하며,
 * Gemini API를 통해 주제 기반 객관식 퀴즈를 자동 생성합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @RestController}로 등록되며, 생성자 주입을 통해 의존성을 주입받습니다.
 *
 * @author Jaewon Ryu
 * @since 2026-04-02
 */

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiQuizController {

    private final AiQuizService aiQuizService;

    @PostMapping("/quizzes")
    public ResponseEntity<Response<AiQuizGenerateResponse>> generateQuiz(
            @RequestParam String topic,
            @AuthenticationPrincipal UserPrincipal principal) {
        AiQuizGenerateResponse response = aiQuizService.generateQuiz(topic, principal.id());
        return ResponseEntity.ok(CommonResponse.success(response, "AI 퀴즈 생성 완료"));
    }
}