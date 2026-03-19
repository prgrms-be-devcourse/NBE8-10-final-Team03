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
 * 퀴즈 세트(QuizSet) 도메인의 비즈니스 로직을 처리하는 서비스입니다.
 * <p>
 * 퀴즈 세트의 생성, 조회, 수정, 삭제 등의 핵심 기능을 제공하며, 트랜잭션을 관리하여 데이터의 정합성을 보장합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@link org.springframework.stereotype.Service} 어노테이션을 통해 스프링 빈으로 관리됩니다. <br>
 * {@link lombok.RequiredArgsConstructor} 를 통한 생성자 주입을 사용합니다. <br>
 *
 * @author MintyU
 * @since 2026-03-19
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizSetService {

    private final QuizSetRepository quizSetRepository;
    private final UserRepository userRepository;

    /**
     * 새로운 퀴즈 세트를 생성하고 저장합니다.
     *
     * @param request 퀴즈 세트 생성 정보
     * @return 생성된 퀴즈 세트 정보
     */
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
