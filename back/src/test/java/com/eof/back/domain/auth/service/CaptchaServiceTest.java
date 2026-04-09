package com.eof.back.domain.auth.service;

import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(captchaService, "secretKey", "test-secret-key");
    }

    @Test
    @DisplayName("토큰이 null이면 예외 발생")
    void verify_Fail_TokenNull() {
        assertThatThrownBy(() -> captchaService.verify(null))
                .isInstanceOf(AuthException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.CAPTCHA_INVALID);
    }

    @Test
    @DisplayName("토큰이 공백이면 예외 발생")
    void verify_Fail_TokenBlank() {
        assertThatThrownBy(() -> captchaService.verify("  "))
                .isInstanceOf(AuthException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.CAPTCHA_INVALID);
    }

    @Test
    @DisplayName("API 호출 중 예외 발생 시 예외 발생")
    void verify_Fail_RestClientException() {
        given(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .willThrow(new RestClientException("Connection failed"));

        assertThatThrownBy(() -> captchaService.verify("some-token"))
                .isInstanceOf(AuthException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.CAPTCHA_INVALID);
    }

    @Test
    @DisplayName("API 응답이 null이면 예외 발생")
    void verify_Fail_ResponseNull() {
        given(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .willReturn(null);

        assertThatThrownBy(() -> captchaService.verify("some-token"))
                .isInstanceOf(AuthException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.CAPTCHA_INVALID);
    }

    @Test
    @DisplayName("API 응답 success가 false이면 예외 발생")
    void verify_Fail_ResponseSuccessFalse() {
        given(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .willReturn(Map.of("success", false));

        assertThatThrownBy(() -> captchaService.verify("some-token"))
                .isInstanceOf(AuthException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.CAPTCHA_INVALID);
    }

    @Test
    @DisplayName("API 응답 success가 true이면 정상 종료")
    void verify_Success() {
        given(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .willReturn(Map.of("success", true));

        assertThatCode(() -> captchaService.verify("valid-token"))
                .doesNotThrowAnyException();
    }
}
