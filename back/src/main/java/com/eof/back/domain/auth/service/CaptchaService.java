package com.eof.back.domain.auth.service;

import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cloudflare Turnstile CAPTCHA 토큰을 검증합니다.
 *
 * <p>로그인 실패 횟수가 임계값에 도달하면 클라이언트로부터 Turnstile 토큰을 받아
 * Cloudflare API로 유효성을 검증합니다.
 *
 * @author 5h6vm
 * @since 2026-04-01
 */

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    static final int CAPTCHA_THRESHOLD = 3;

    @Value("${custom.turnstile.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;

    /**
     * Turnstile 토큰을 Cloudflare API로 검증합니다.
     *
     * @param token 클라이언트에서 전달받은 Turnstile 토큰
     * @throws AuthException 토큰이 null이거나 검증 실패 시 CAPTCHA_INVALID
     */
    public void verify(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthException(AuthErrorCode.CAPTCHA_INVALID);
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", token);

        Map<?, ?> response;
        try {
            response = restTemplate.postForObject(TURNSTILE_VERIFY_URL, body, Map.class);
        } catch (RestClientException e) {
            throw new AuthException(AuthErrorCode.CAPTCHA_INVALID);
        }

        if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
            throw new AuthException(AuthErrorCode.CAPTCHA_INVALID);
        }
    }
}
