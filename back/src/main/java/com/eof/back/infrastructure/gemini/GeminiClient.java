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

    public String call(String topic) {
        String prompt = """
            주제: %s
            위 주제로 객관식 퀴즈 5개를 만들어줘.
            단, 폭력적이거나 선정적이거나 혐오적인 주제는 거부하고 빈 배열 []만 반환해.
            반드시 아래 JSON 형식으로만 응답해:
            [
              {
                "content": "문제 내용",
                "answer": "정답",
                "choice1": "선택지1",
                "choice2": "선택지2",
                "choice3": "선택지3",
                "choice4": "선택지4"
              }
            ]
            JSON 외에 다른 텍스트는 절대 포함하지 마.
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