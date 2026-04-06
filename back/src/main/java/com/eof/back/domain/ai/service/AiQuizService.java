package com.eof.back.domain.ai.service;

import com.eof.back.domain.ai.dto.AiQuizGenerateResponse;
import com.eof.back.domain.ai.dto.AiQuizResponse;

import java.util.List;

/**
 * AI 퀴즈 생성 기능을 정의하는 서비스 인터페이스입니다.
 * <p>
 * Gemini API를 활용하여 주제 기반 객관식 퀴즈를 생성하고
 * QuizSet으로 저장하는 기능을 제공합니다.
 *
 * @author Jaewon Ryu
 * @see AiQuizServiceImpl
 * @since 2026-04-02
 */
public interface AiQuizService {
    AiQuizGenerateResponse generateQuiz(String topic, Long userId);
}