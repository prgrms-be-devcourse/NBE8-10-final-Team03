package com.eof.back.domain.quiz.service;


import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.QuizSetException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;

    @Override
    public Long createQuiz(Long quizSetId, QuizCreateRequest request) {
        QuizSet quizSet = quizSetRepository.findById(quizSetId).orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND,
                "[QuizServiceImpl#createQuiz] can't find quiz set with id: " + quizSetId, "존재하지 않는 퀴즈 세트입니다."));

        Quiz quiz = Quiz.builder()
                .quizSet(quizSet)
                .content(request.content())
                .answer(request.answer())
                .choice1(request.choice1())
                .choice2(request.choice2())
                .choice3(request.choice3())
                .choice4(request.choice4())
                .build();

        return quizRepository.save(quiz).getId();
    }
}
