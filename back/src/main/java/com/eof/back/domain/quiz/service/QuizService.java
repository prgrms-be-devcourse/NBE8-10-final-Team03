package com.eof.back.domain.quiz.service;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;

public interface QuizService {
    Long createQuiz(Long quizSetId, QuizCreateRequest request);
}
