package com.eof.back.domain.ai.service;

import com.eof.back.domain.ai.dto.AiQuizGenerateResponse;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.AuthProvider;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.exceptions.QuizSetException;
import com.eof.back.infrastructure.gemini.GeminiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AiQuizServiceImplTest {

    @InjectMocks
    private AiQuizServiceImpl aiQuizService;  // @Spy 제거

    @Mock
    private GeminiClient geminiClient;  // 추가

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizSetRepository quizSetRepository;

    @Mock
    private QuizRepository quizRepository;

    @Test
    @DisplayName("AI 퀴즈 생성 - 정상")
    void generateQuiz_success() {
        String geminiResponse = """
                {
                  "candidates": [{
                    "content": {
                      "parts": [{
                        "text": "[{\\"content\\": \\"문제1\\", \\"answer\\": \\"정답1\\", \\"choice1\\": \\"정답1\\", \\"choice2\\": \\"보기2\\", \\"choice3\\": \\"보기3\\", \\"choice4\\": \\"보기4\\"}]"
                      }]
                    }
                  }]
                }
                """;

        User user = createUser();
        QuizSet quizSet = QuizSet.of("[AI] 한국사", "AI가 생성한 한국사 퀴즈", user);

        given(geminiClient.call(anyString())).willReturn(geminiResponse);  // 변경
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(quizSetRepository.save(any())).willReturn(quizSet);

        AiQuizGenerateResponse response = aiQuizService.generateQuiz("한국사", 1L);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("AI 퀴즈 생성 - 부적절한 주제로 빈 배열 반환 시 예외")
    void generateQuiz_emptyResponse_throwsException() {
        String geminiResponse = """
                {
                  "candidates": [{
                    "content": {
                      "parts": [{
                        "text": "[]"
                      }]
                    }
                  }]
                }
                """;

        given(geminiClient.call(anyString())).willReturn(geminiResponse);  // 변경

        assertThatThrownBy(() -> aiQuizService.generateQuiz("부적절한주제", 1L))
                .isInstanceOf(QuizSetException.class);
    }

    private User createUser() {
        return User.builder()
                .username("testuser")
                .password("test1234")
                .nickname("테스트맨")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .build();
    }
}

