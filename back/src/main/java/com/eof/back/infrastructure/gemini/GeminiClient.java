package com.eof.back.infrastructure.gemini;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Gemini API 호출을 담당하는 클라이언트입니다.
 * <p>
 * {@code AiQuizServiceImpl}에서 분리된 외부 API 통신 전담 클래스로,
 * 주제를 입력받아 Gemini API에 퀴즈 생성을 요청하고 응답 문자열을 반환합니다.
 * 프롬프트 조립, HTTP 요청, 타임아웃 처리를 담당합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Component}로 등록되며, 생성자 주입을 통해 의존성을 주입받습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring WebFlux의 {@link WebClient}를 사용하여 Gemini API와 통신합니다.
 * 서비스 레이어가 MVC 기반이므로 {@code .block()}으로 동기 처리합니다.
 *
 * @author Jaewon Ryu
 * @since 2026-04-07
 */

@Component
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    public String call(String topic) {  // callGeminiApi → call로 이름 변경
        String prompt = """
                주제: %s
                위 주제로 객관식 퀴즈 5개를 만들어줘.
                ...
                """.formatted(topic);

        String requestBody = """
                {
                  "contents": [{
                    "parts": [{"text": "%s"}]
                  }]
                }
                """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        return webClient.post()
                .uri("/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
    }
}