package com.eof.back.admin.service;

import com.eof.back.admin.dto.AdminUserResponse;
import com.eof.back.admin.entity.UserSuspension;
import com.eof.back.admin.repository.UserSuspensionRepository;
import com.eof.back.domain.auth.store.RefreshTokenStore;
import com.eof.back.global.token.TokenVersionStore;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizreport.dto.QuizReportResponse;
import com.eof.back.domain.quizreport.entity.QuizReport;
import com.eof.back.domain.quizreport.entity.QuizReportStatus;
import com.eof.back.domain.quizreport.repository.QuizReportRepository;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.gamerecord.repository.GameRecordRepository;
import com.eof.back.domain.user.quizsetbookmark.repository.QuizSetBookmarkRepository;
import com.eof.back.domain.gamesession.repository.GameSessionRepository;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizErrorCode;
import com.eof.back.global.exception.errorCode.QuizReportErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizException;
import com.eof.back.global.exception.exceptions.QuizReportException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link AdminService} 인터페이스의 구현체로, 관리자 전용 비즈니스 로직을 수행합니다.
 * <p>
 * 모든 실행 로직에 앞서 요청자의 식별자를 기반으로 {@link Role#ADMIN} 권한 보유 여부를 검증합니다.
 * </p>
 *
 * @author MintyU
 * @since 2026-03-31
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizReportRepository quizReportRepository;
    private final UserRepository userRepository;
    private final UserSuspensionRepository userSuspensionRepository;
    
    private final QuizSetBookmarkRepository quizSetBookmarkRepository;
    private final GameRecordRepository gameRecordRepository;
    private final GameSessionRepository gameSessionRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenVersionStore tokenVersionStore;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Long updateQuizSet(Long id, QuizSetUpdateRequest request, Long adminId) {
        validateAdminRole(adminId);
        QuizSet quizSet = findQuizSetById(id);
        quizSet.update(request.title(), request.description());
        return quizSet.getId();
    }

    /**
     * {@inheritDoc}
     * 모든 연관 데이터를 벌크 연산으로 정리하여 무결성을 유지합니다.
     */
    @Override
    @Transactional
    public void deleteQuizSet(Long id, Long adminId) {
        validateAdminRole(adminId);
        if (!quizSetRepository.existsById(id)) {
            throw new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND);
        }
        
        quizSetBookmarkRepository.deleteByQuizSetId(id);
        gameRecordRepository.deleteByQuizSetId(id);
        gameSessionRepository.deleteByQuizSetId(id);
        quizRepository.deleteByQuizSetId(id);
        quizSetRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Long updateQuiz(Long quizSetId, Long quizId, QuizUpdateRequest request, Long adminId) {
        validateAdminRole(adminId);
        Quiz quiz = findQuizById(quizId);
        
        validatePathConsistency(quiz, quizSetId);

        quiz.update(
                request.questionType(),
                request.answerType(),
                request.content(),
                request.answer(),
                request.imageUrl(),
                request.videoUrl(),
                request.startTime(),
                request.endTime(),
                request.choice1(),
                request.choice2(),
                request.choice3(),
                request.choice4()
        );
        return quiz.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteQuiz(Long quizSetId, Long quizId, Long adminId) {
        validateAdminRole(adminId);
        Quiz quiz = findQuizById(quizId);
        
        validatePathConsistency(quiz, quizSetId);

        QuizSet quizSet = quiz.getQuizSet();
        quizRepository.delete(quiz);
        quizSet.decreaseQuizCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuizReportResponse getReport(Long id, Long adminId) {
        validateAdminRole(adminId);
        QuizReport quizReport = quizReportRepository.findById(id)
                .orElseThrow(() -> new QuizReportException(QuizReportErrorCode.QUIZ_REPORT_NOT_FOUND));
        return QuizReportResponse.from(quizReport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuizReportResponse> getAllReports(Long adminId) {
        validateAdminRole(adminId);
        return quizReportRepository.findAll().stream()
                .map(QuizReportResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void processReport(Long id, Long adminId) {
        validateAdminRole(adminId);
        QuizReport quizReport = quizReportRepository.findById(id)
                .orElseThrow(() -> new QuizReportException(QuizReportErrorCode.QUIZ_REPORT_NOT_FOUND));
        
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
    public void deleteReport(Long id, Long adminId) {
        validateAdminRole(adminId);
        if (!quizReportRepository.existsById(id)) {
            throw new QuizReportException(QuizReportErrorCode.QUIZ_REPORT_NOT_FOUND);
        }
        quizReportRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     * 정지 기간과 사유를 UserSuspension에 기록하고 사용자의 상태를 업데이트합니다.
     */
    @Override
    @Transactional
    public void suspendUser(Long targetUserId, String reason, int suspensionDays, Long adminId) {
        validateAdminRole(adminId);
        User user = findUserById(targetUserId);
        
        user.suspend();

        // 기존 토큰 즉시 무효화: version 증가 + refresh token 삭제
        tokenVersionStore.increment(targetUserId);
        refreshTokenStore.delete(targetUserId);

        LocalDateTime until = LocalDateTime.now().plusDays(suspensionDays);

        userSuspensionRepository.findByUserId(targetUserId)
                .ifPresentOrElse(
                        suspension -> suspension.update(reason, until),
                        () -> userSuspensionRepository.save(UserSuspension.of(user, reason, until))
                );
    }

    /**
     * {@inheritDoc}
     * 사용자의 상태를 DELETED로 변경하며, 활성 상태인 정지 정보를 삭제합니다.
     */
    @Override
    @Transactional
    public void deleteUser(Long targetUserId, Long adminId) {
        validateAdminRole(adminId);
        User user = findUserById(targetUserId);
        
        user.delete();
        userSuspensionRepository.deleteByUserId(targetUserId);

        // 기존 토큰 즉시 무효화: version 삭제 + refresh token 삭제
        tokenVersionStore.delete(targetUserId);
        refreshTokenStore.delete(targetUserId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Slice<AdminUserResponse> getUsers(String keyword, Pageable pageable, Long adminId) {
        validateAdminRole(adminId);

        Slice<User> users;
        if (keyword != null && !keyword.isBlank()) {
            users = userRepository.findByNicknameContaining(keyword, pageable);
        } else {
            users = userRepository.findAllBy(pageable);
        }

        return users.map(AdminUserResponse::from);
    }

    /**
     * 실행 주체가 관리자(ADMIN) 권한을 가졌는지 엄격히 검증합니다.
     *
     * @param userId 요청자 식별자
     */
    private void validateAdminRole(Long userId) {
        User user = findUserById(userId);
        if (user.getRole() != Role.ADMIN) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
    }

    private QuizSet findQuizSetById(Long id) {
        return quizSetRepository.findById(id)
                .orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));
    }

    private Quiz findQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_FOUND));
    }

    private void validatePathConsistency(Quiz quiz, Long quizSetId) {
        if (!quiz.getQuizSet().getId().equals(quizSetId)) {
            throw new QuizException(QuizErrorCode.QUIZ_NOT_FOUND);
        }
    }
}
