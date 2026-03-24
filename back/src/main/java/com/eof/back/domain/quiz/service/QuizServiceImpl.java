package com.eof.back.domain.quiz.service;


import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.global.exception.errorCode.QuizErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.QuizException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link QuizService} 인터페이스의 구현체입니다.
 * <p>
 * 퀴즈의 CRUD 비즈니스 로직을 수행하며, 퀴즈 세트와의 연관 관계 및 메타데이터 동기화를 관리합니다.
 * </p>
 *
 * @author MintyU
 * @since 2026-03-24
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;

    /**
     * {@inheritDoc}
     * <p>
     * 새로운 퀴즈를 생성하여 지정된 퀴즈 세트에 추가합니다.
     * 저장 후 해당 퀴즈 세트의 총 문제 수({@code totalQuizCount})를 1 증가시킵니다.
     * </p>
     */
    @Override
    @Transactional
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

        Quiz savedQuiz = quizRepository.save(quiz);
        quizSet.increaseQuizCount();

        return savedQuiz.getId();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 지정된 식별자로 퀴즈를 조회하여 반환합니다.
     * </p>
     */
    @Override
    public QuizResponse getQuiz(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        return QuizResponse.from(quiz);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 특정 퀴즈 세트에 속한 모든 퀴즈 목록을 조회하여 반환합니다.
     * </p>
     */
    @Override
    public List<QuizResponse> getQuizzesByQuizSetId(Long quizSetId) {
        QuizSet quizSet = quizSetRepository.findById(quizSetId).orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND,
                "[QuizServiceImpl#getQuizzesByQuizSetId] can't find quiz set with id: " + quizSetId));
        
        return quizSet.getQuizzes().stream()
                .map(QuizResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * 기존 퀴즈의 정보를 부분적으로 수정합니다. (PATCH)
     * </p>
     */
    @Override
    @Transactional
    public Long updateQuiz(Long quizId, QuizUpdateRequest request) {
        Quiz quiz = findQuizById(quizId);
        quiz.update(
                request.content(),
                request.answer(),
                request.choice1(),
                request.choice2(),
                request.choice3(),
                request.choice4()
        );
        return quiz.getId();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 퀴즈를 삭제하고, 해당 퀴즈 세트의 총 문제 수({@code totalQuizCount})를 1 감소시킵니다.
     * </p>
     */
    @Override
    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        QuizSet quizSet = quiz.getQuizSet();
        
        quizRepository.delete(quiz);
        quizSet.decreaseQuizCount();
    }

    /**
     * 식별자로 퀴즈 엔티티를 조회합니다. 존재하지 않을 경우 예외를 발생시킵니다.
     *
     * @param quizId 조회할 퀴즈 식별자
     * @return 퀴즈 엔티티
     * @throws QuizException 퀴즈가 존재하지 않을 경우
     */
    private Quiz findQuizById(Long quizId) {
        return quizRepository.findById(quizId).orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_FOUND,
                "[QuizServiceImpl] can't find quiz with id: " + quizId));
    }
}
