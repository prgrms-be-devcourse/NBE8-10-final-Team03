package com.eof.back.domain.quizset.service;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetCreateResponse;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
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
        // 현재는 임시로 ID가 1L인 사용자를 제작자로 설정하거나, 없을 경우 에러 처리가 필요하나
        // 여기서는 임시 Mock 로직으로 처리합니다.
        User creator = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("임시 사용자(ID: 1)를 찾을 수 없습니다."));

        QuizSet quizSet = QuizSet.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .creator(creator)
                .totalQuizCount(request.getTotalQuestionCount())
                .build();

        QuizSet savedQuizSet = quizSetRepository.save(quizSet);

        return QuizSetCreateResponse.from(savedQuizSet);
    }
}
