package com.eof.back.domain.quizset.service;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetCreateResponse;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
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
     * 현재 구현에서는 ID가 1L인 임시 사용자를 제작자로 설정하며,
     * 전달된 요청 데이터를 기반으로 {@link QuizSet} 엔티티를 생성하고 저장합니다.
     */
    @Override
    @Transactional
    public QuizSetCreateResponse createQuizSet(QuizSetCreateRequest request) {
        // TODO: 인증 기능 구현 후 현재 로그인된 사용자 정보를 가져오도록 수정
        User creator = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("임시 사용자(ID: 1)를 찾을 수 없습니다."));

        QuizSet quizSet = QuizSet.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .creator(creator)
                .totalQuizCount(request.getTotalQuizCount())
                .build();

        QuizSet savedQuizSet = quizSetRepository.save(quizSet);

        return QuizSetCreateResponse.from(savedQuizSet);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 지정된 ID로 {@link QuizSetRepository}에서 조회하며,
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
}
