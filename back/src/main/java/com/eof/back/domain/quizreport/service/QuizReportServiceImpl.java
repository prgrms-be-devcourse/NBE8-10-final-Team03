package com.eof.back.domain.quizreport.service;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.entity.QuizReport;
import com.eof.back.domain.quizreport.repository.QuizReportRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link QuizReportService} 인터페이스의 기본 구현체입니다.
 * 관리자 전용 기능은 AdminService로 이관되었습니다.
 *
 * @author MintyU
 * @since 2026-03-27
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizReportServiceImpl implements QuizReportService {

    private final QuizReportRepository quizReportRepository;
    private final QuizSetRepository quizSetRepository;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Long createReport(QuizReportCreateRequest request, Long userId) {
        User reporter = findUserById(userId);

        QuizSet quizSet = quizSetRepository.findById(request.getQuizSetId())
                .orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));

        QuizReport quizReport = QuizReport.of(reporter, quizSet, request.getReason());
        QuizReport savedReport = quizReportRepository.save(quizReport);

        return savedReport.getId();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
    }
}
