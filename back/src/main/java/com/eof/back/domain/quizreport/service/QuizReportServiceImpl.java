package com.eof.back.domain.quizreport.service;

import com.eof.back.domain.quizreport.dto.QuizReportCreateRequest;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import com.eof.back.domain.quizreport.entity.QuizReport;
import com.eof.back.domain.quizreport.entity.QuizReportStatus;
import com.eof.back.domain.quizreport.repository.QuizReportRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizReportErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizReportException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link QuizReportService} 인터페이스의 기본 구현체입니다.
 * <p>
 * 퀴즈 신고(QuizReport) 도메인의 비즈니스 로직을 수행하며, 신고 생성, 조회, 처리 상태 업데이트, 삭제 등의 기능을 제공합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link QuizReportService} 인터페이스를 구현합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code QuizReportServiceImpl(QuizReportRepository, QuizSetRepository, UserRepository)} <br>
 * 생성자 주입을 위한 생성자로, Lombok의 {@link RequiredArgsConstructor}에 의해 자동 생성됩니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * Spring Boot의 {@link Service}에 의해 Bean으로 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Transaction을 사용하여 트랜잭션을 관리합니다.
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
     * <p>
     * 제공된 퀴즈 세트 ID와 사용자 ID를 기반으로 신고를 생성합니다.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public QuizReportResponse getReport(Long id, Long userId) {
        validateAdminRole(userId);
        QuizReport quizReport = findQuizReportById(id);
        return QuizReportResponse.from(quizReport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuizReportResponse> getAllReports(Long userId) {
        validateAdminRole(userId);
        return quizReportRepository.findAll().stream()
                .map(QuizReportResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * 신고 상태가 이미 '처리 완료'인 경우 예외를 발생시킵니다.
     */
    @Override
    @Transactional
    public void processReport(Long id, Long userId) {
        validateAdminRole(userId);
        QuizReport quizReport = findQuizReportById(id);
        
        if (quizReport.getStatus() == QuizReportStatus.PROCESSED) {
            throw new QuizReportException(QuizReportErrorCode.QUIZ_REPORT_ALREADY_PROCESSED);
        }

        quizReport.process();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteReport(Long id, Long userId) {
        validateAdminRole(userId);
        if (!quizReportRepository.existsById(id)) {
            throw new QuizReportException(QuizReportErrorCode.QUIZ_REPORT_NOT_FOUND);
        }
        quizReportRepository.deleteById(id);
    }

    /**
     * 내부적으로 신고 내역을 조회하기 위한 헬퍼 메서드입니다.
     *
     * @param id 조회할 신고의 식별자
     * @return 발견된 QuizReport 엔티티
     * @throws QuizReportException 신고 내역이 존재하지 않을 경우
     */
    private QuizReport findQuizReportById(Long id) {
        return quizReportRepository.findById(id)
                .orElseThrow(() -> new QuizReportException(QuizReportErrorCode.QUIZ_REPORT_NOT_FOUND));
    }

    /**
     * 사용자가 관리자 권한을 가졌는지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @throws AuthException 관리자 권한이 없거나 사용자를 찾을 수 없을 경우
     */
    private void validateAdminRole(Long userId) {
        User user = findUserById(userId);
        if (user.getRole() != Role.ADMIN) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL);
        }
    }

    /**
     * ID로 사용자를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 발견된 User 엔티티
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
    }
}
