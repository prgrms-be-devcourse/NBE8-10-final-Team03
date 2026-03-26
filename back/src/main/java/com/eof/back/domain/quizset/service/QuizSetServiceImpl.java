package com.eof.back.domain.quizset.service;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

    /**
     * {@inheritDoc}
     * <p>
     * 제공된 사용자 ID를 기반으로 {@link QuizSet} 엔티티를 생성하고 저장합니다.
     * 사용자가 존재하지 않으면 {@link AuthException}을 발생시킵니다.
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
     * 지정된 ID로 {@link QuizSetRepository}에서 조회합니다.
     * 해당 엔티티가 존재하지 않을 경우 {@link QuizSetException}을 발생시킵니다.
     */
    @Override
    public QuizSetResponse getQuizSet(Long id) {
        QuizSet quizSet = quizSetRepository.findById(id)
                .orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));
        return QuizSetResponse.from(quizSet);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 전체 {@link QuizSet} 엔티티를 조회한 후, 요약 정보만 포함된 {@link QuizSetListResponse} 목록으로 변환합니다.
     */
    @Override
    public List<QuizSetListResponse> getAllQuizSets() {
        return quizSetRepository.findAll().stream()
                .map(QuizSetListResponse::from)
                .collect(Collectors.toList());
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
        // 1. 존재 여부 확인 (엔티티 전체 로드 없이 ID만 조회하여 메모리/네트워크 비용 절감)
        if (!quizSetRepository.existsById(id)) {
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND);
        }

        // 2. ID 기반 즉시 삭제 (Bulk Delete 실행)
        // 소유권 확인(userId)을 쿼리에 포함하여 권한 검증과 삭제를 원자적으로 수행합니다.
        // @OnDelete(CASCADE) 설정 덕분에 하이버네이트의 추가적인 연관 엔티티 조회 없이 DB 레벨에서 모든 연관 데이터가 삭제됩니다.
        int deletedCount = quizSetRepository.deleteByIdAndCreatorId(id, userId);

        // 3. 삭제된 행이 없다면 제작자가 아님을 의미하므로 권한 예외 발생
        if (deletedCount == 0) {
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_ACCESS_DENIED);
        }
    }

    /**
     * 내부적으로 사용되는 퀴즈 세트 조회 도구입니다.
     *
     * @param id 조회할 ID
     * @return 발견된 퀴즈 세트 엔티티
     * @throws QuizSetException 엔티티가 존재하지 않을 경우
     */
    private QuizSet findQuizSetById(Long id) {
        return quizSetRepository.findById(id)
                .orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));
    }

    /**
     * 사용자 권한을 검증합니다. 퀴즈 세트의 제작자만 수정/삭제가 가능합니다.
     *
     * @param quizSet 대상 퀴즈 세트
     * @param userId  요청 사용자 ID
     * @throws QuizSetException 제작자가 아닐 경우 (ACCESS_DENIED)
     */
    private void validateOwnership(QuizSet quizSet, Long userId) {
        if (!quizSet.getCreator().getId().equals(userId)) {
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_ACCESS_DENIED);
        }
    }
}
