package com.eof.back.domain.quizset.service;

import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.quizsetbookmark.repository.QuizSetBookmarkRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link QuizSetService} 인터페이스의 구현체입니다.
 * <p>
 * 퀴즈 세트의 생성, 조회, 수정, 삭제 등의 비즈니스 로직을 실제로 수행하며,
 * 데이터베이스와의 상호작용 및 트랜잭션을 관리합니다.
 *
 * @author MintyU
 * @since 2026-03-19
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizSetServiceImpl implements QuizSetService {

    private final QuizSetRepository quizSetRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizSetBookmarkRepository quizSetBookmarkRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameRecordRepository gameRecordRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Long createQuizSet(QuizSetCreateRequest request, Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        QuizSet quizSet = QuizSet.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .creator(creator)
                .build();

        QuizSet savedQuizSet = quizSetRepository.save(quizSet);

        return savedQuizSet.getId();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 작성자 본인 여부를 검증한 후 정보를 반환합니다.
     */
    @Override
    public QuizSetResponse getQuizSet(Long id, Long userId) {
        QuizSet quizSet = findQuizSetById(id);
        validateOwnership(quizSet, userId);
        return QuizSetResponse.from(quizSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Slice<QuizSetListResponse> getAllQuizSets(Pageable pageable) {
        return quizSetRepository.findAllWithCreator(pageable)
                .map(QuizSetListResponse::from);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Long updateQuizSet(Long id, QuizSetUpdateRequest request, Long userId) {
        QuizSet quizSet = findQuizSetById(id);
        validateOwnership(quizSet, userId);

        quizSet.update(request.title(), request.description());
        return quizSet.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteQuizSet(Long id, Long userId) {
        // 1. 참조 엔티티 정리
        quizSetBookmarkRepository.deleteByQuizSetId(id);
        gameRecordRepository.deleteByQuizSetId(id);
        gameSessionRepository.deleteByQuizSetId(id);
        
        // 2. 세트 내 개별 퀴즈 삭제
        quizRepository.deleteByQuizSetId(id);

        // 3. 원자적 삭제 시도 (소유권 확인 포함)
        int deletedCount = quizSetRepository.deleteByIdAndCreatorId(id, userId);

        if (deletedCount == 0) {
            if (!quizSetRepository.existsById(id)) {
                throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND);
            }
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_ACCESS_DENIED);
        }
    }

    private QuizSet findQuizSetById(Long id) {
        return quizSetRepository.findById(id)
                .orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));
    }

    private void validateOwnership(QuizSet quizSet, Long userId) {
        if (!quizSet.getCreator().getId().equals(userId)) {
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_ACCESS_DENIED);
        }
    }
}
