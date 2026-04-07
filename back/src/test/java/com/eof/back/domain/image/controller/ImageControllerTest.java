package com.eof.back.domain.image.controller;

import com.eof.back.domain.image.dto.ImageUploadResponse;
import com.eof.back.domain.image.service.ImageService;
import com.eof.back.global.exception.errorCode.ImageErrorCode;
import com.eof.back.global.exception.exceptions.ImageException;
import com.eof.back.global.exception.exceptionHadler.DefaultExceptionHandler;
import com.eof.back.global.jwt.CookieUtil;
import com.eof.back.global.jwt.JwtAuthenticationEntryPoint;
import com.eof.back.global.jwt.JwtTokenProvider;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.token.TokenVersionStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ImageController의 단위 테스트입니다.
 * <p>
 * uploadImage 엔드포인트의 성공 및 실패 시나리오를 검증합니다.
 *
 * @author Minji-032
 * @since 2026-04-06
 */
@ActiveProfiles("test")
@Import({ImageControllerTest.MockSecurityConfig.class, DefaultExceptionHandler.class})
@WebMvcTest(ImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageService imageService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private TokenVersionStore tokenVersionStore;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Nested
    @DisplayName("uploadImage")
    class UploadImage {

        @Test
        @DisplayName("성공 - 이미지 업로드 후 200과 accessUrl을 반환한다")
        void success() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[100]);
            given(imageService.uploadImage(any(), anyLong()))
                    .willReturn(new ImageUploadResponse("https://s3.../uuid.jpg"));

            // when & then
            mockMvc.perform(multipart("/api/v1/images").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessUrl").value("https://s3.../uuid.jpg"));
        }

        @Test
        @DisplayName("실패 - 빈 파일이면 400을 반환한다")
        void fail_emptyFile() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
            given(imageService.uploadImage(any(), anyLong()))
                    .willThrow(new ImageException(ImageErrorCode.IMAGE_EMPTY));

            // when & then
            mockMvc.perform(multipart("/api/v1/images").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ImageErrorCode.IMAGE_EMPTY.getMessage()));
        }

        @Test
        @DisplayName("실패 - 허용되지 않는 확장자면 400을 반환한다")
        void fail_invalidExtension() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.webp", "image/webp", new byte[100]);
            given(imageService.uploadImage(any(), anyLong()))
                    .willThrow(new ImageException(ImageErrorCode.IMAGE_INVALID_EXTENSION));

            // when & then
            mockMvc.perform(multipart("/api/v1/images").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ImageErrorCode.IMAGE_INVALID_EXTENSION.getMessage()));
        }

        @Test
        @DisplayName("실패 - 업로드 실패 시 500을 반환한다")
        void fail_uploadFailed() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[100]);
            given(imageService.uploadImage(any(), anyLong()))
                    .willThrow(new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAILED));

            // when & then
            mockMvc.perform(multipart("/api/v1/images").file(file))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value(ImageErrorCode.IMAGE_UPLOAD_FAILED.getMessage()));
        }
    }

    @TestConfiguration
    static class MockSecurityConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(UserPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {
                    return new UserPrincipal(1L, "testuser", "tester", "USER");
                }
            });
        }
    }
}
