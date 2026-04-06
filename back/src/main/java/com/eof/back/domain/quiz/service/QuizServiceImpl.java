package com.eof.back.domain.quiz.service;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import com.eof.back.domain.quiz.entity.AnswerType;
import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.errorCode.QuizErrorCode;
import com.eof.back.global.exception.errorCode.QuizSetErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import com.eof.back.global.exception.exceptions.QuizException;
import com.eof.back.global.exception.exceptions.QuizSetException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link QuizService} 인터페이스의 기본 구현체입니다.
 * <p>
 * 퀴즈(Quiz) 도메인의 핵심 비즈니스 로직을 수행하며, 퀴즈 세트(QuizSet)와의 강한 연관 관계를 관리합니다.
 * 모든 변경 작업(생성, 수정, 삭제) 시 데이터 무결성을 보장하기 위해 경로 일관성 검증과 권한 검증을 엄격히 수행합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link QuizService} 인터페이스를 구현하며, 퀴즈의 생명주기를 관리합니다.
 *
 * <p><b>주요 기능:</b><br>
 * - <b>동적 동기화:</b> 퀴즈 생성/삭제 시 소속 퀴즈 세트의 {@code quizCount} 필드를 자동으로 업데이트합니다.<br>
 * - <b>보안 검증:</b> 모든 요청에 대해 제작자 권한({@code Ownership})을 확인하여 불법적인 데이터 변경을 차단합니다.<br>
 * - <b>일관성 검증:</b> API 경로의 {@code quizSetId}와 실제 퀴즈의 소속 세트가 일치하는지 대조하여 논리적 오류를 방지합니다.
 *
 * <p><b>트랜잭션 관리:</b><br>
 * {@code @Transactional(readOnly = true)}를 기본으로 하여 조회 성능을 최적화하고,
 * 상태를 변경하는 메서드에는 {@code @Transactional}을 선언하여 원자적 연산을 보장합니다.
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
     * <b>실행 흐름:</b>
     * <ol>
     *     <li>{@code quizSetId}를 기반으로 대상 퀴즈 세트를 조회합니다.</li>
     *     <li>현재 요청 사용자({@code userId})가 해당 세트의 소유자인지 검증합니다.</li>
     *     <li>제공된 {@code request} 데이터를 기반으로 퀴즈 엔티티를 생성합니다.</li>
     *     <li>퀴즈를 저장소에 등록하고, 연관된 퀴즈 세트의 문제 수를 증가시킵니다.</li>
     * </ol>
     * </p>
     *
     * @param quizSetId 퀴즈를 추가할 대상 퀴즈 세트의 식별자
     * @param request   생성할 퀴즈의 데이터(내용, 정답, 선택지 등)
     * @param userId    현재 요청자의 식별자 (권한 검증용)
     * @return 생성된 퀴즈의 ID
     * @throws QuizSetException 대상 퀴즈 세트가 존재하지 않을 경우
     * @throws AuthException    요청자가 퀴즈 세트의 제작자가 아닐 경우
     */
    @Override
    @Transactional
    public Long createQuiz(Long quizSetId, QuizCreateRequest request, Long userId) {
        QuizSet quizSet = findQuizSetById(quizSetId);

        // 권한 검증: 퀴즈 세트 제작자만 퀴즈를 추가할 수 있음
        validateOwnership(quizSet, userId);

        // 객관식 선택지 검증
        validateMultipleChoiceOptions(request.answerType(), request.choice1(), request.choice2(), request.choice3(), request.choice4());

        Quiz quiz = Quiz.builder()
                .quizSet(quizSet)
                .questionType(request.questionType())
                .answerType(request.answerType())
                .content(request.content())
                .answer(request.answer())
                .imageUrl(request.imageUrl())
                .videoUrl(request.videoUrl())
                .startTime(request.startTime())
                .endTime(request.endTime())
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
     * <b>보안 고려사항:</b>
     * 단순히 퀴즈 ID로만 조회하지 않고, 해당 퀴즈가 실제로 명시된 {@code quizSetId}에
     * 속해 있는지 대조하여 URL 조작을 통한 비정상적인 접근을 방지합니다.
     * </p>
     *
     * @param quizSetId 퀴즈가 속한 것으로 기대되는 퀴즈 세트의 ID
     * @param quizId    조회할 퀴즈의 고유 식별자
     * @return 퀴즈 상세 정보를 담은 응답 DTO
     * @throws QuizException 퀴즈를 찾을 수 없거나 세트 정보가 불일치할 경우
     */
    @Override
    public QuizResponse getQuiz(Long quizSetId, Long quizId) {
        Quiz quiz = findQuizById(quizId);

        // 일관성 검증: 해당 퀴즈가 요청된 퀴즈 세트에 속하는지 확인
        validatePathConsistency(quiz, quizSetId);

        return QuizResponse.from(quiz);
    }

    /**
     * {@inheritDoc}
     *
     * @param quizSetId 조회 대상 퀴즈 세트의 ID
     * @return 해당 세트 내 모든 퀴즈의 목록 (DTO 형태)
     * @throws QuizSetException 퀴즈 세트를 찾을 수 없을 경우
     */
    @Override
    public List<QuizResponse> getQuizzesByQuizSetId(Long quizSetId) {
        QuizSet quizSet = findQuizSetById(quizSetId);

        return quizSet.getQuizzes().stream()
                .map(QuizResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>수정 정책:</b>
     * - 퀴즈 내용, 정답, 선택지 전체를 새로운 값으로 업데이트합니다.<br>
     * - 수정 작업 전 소유권(Ownership) 및 경로 일관성을 모두 검증합니다.
     * </p>
     *
     * @param quizSetId 퀴즈가 속한 퀴즈 세트의 ID
     * @param quizId    수정할 대상 퀴즈의 ID
     * @param request   업데이트할 데이터
     * @param userId    현재 요청자의 식별자 (권한 검증용)
     * @return 수정된 퀴즈의 ID
     * @throws QuizException 퀴즈를 찾을 수 없거나 일관성 검증 실패 시
     * @throws AuthException 수정 권한이 없을 시
     */
    @Override
    @Transactional
    public Long updateQuiz(Long quizSetId, Long quizId, QuizUpdateRequest request, Long userId) {
        Quiz quiz = findQuizById(quizId);

        // 일관성 검증
        validatePathConsistency(quiz, quizSetId);
        // 권한 검증
        validateOwnership(quiz.getQuizSet(), userId);

        // 객관식 선택지 검증 (수정 시에도 유형이 변경되거나 선택지가 변경될 수 있으므로 검증 필요)
        // null인 필드는 기존 값을 유지하므로, 검증 시 로직 주의 필요
        // 여기서는 request에 담긴 값이 있을 때만 검증하거나, 최종 결과물을 검증하는 방식이 좋음
        // record의 특성상 request의 answerType이 null일 수 있으므로 null 체크 필수
        AnswerType finalAnswerType = request.answerType() != null ? request.answerType() : quiz.getAnswerType();
        if (finalAnswerType == AnswerType.MULTIPLE_CHOICE) {
            String c1 = request.choice1() != null ? request.choice1() : quiz.getChoice1();
            String c2 = request.choice2() != null ? request.choice2() : quiz.getChoice2();
            String c3 = request.choice3() != null ? request.choice3() : quiz.getChoice3();
            String c4 = request.choice4() != null ? request.choice4() : quiz.getChoice4();
            validateMultipleChoiceOptions(finalAnswerType, c1, c2, c3, c4);
        }

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
     * <p>
     * <b>데이터 후처리:</b>
     * 퀴즈가 성공적으로 삭제되면, 해당 퀴즈가 속해 있던 퀴즈 세트의 {@code quizCount}를
     * 원자적으로 감소시켜 통계 데이터의 무결성을 유지합니다.
     * </p>
     *
     * @param quizSetId 퀴즈가 속한 퀴즈 세트의 ID
     * @param quizId    삭제할 대상 퀴즈의 ID
     * @param userId    현재 요청자의 식별자 (권한 검증용)
     * @throws QuizException 퀴즈를 찾을 수 없거나 일관성 검증 실패 시
     * @throws AuthException 삭제 권한이 없을 시
     */
    @Override
    @Transactional
    public void deleteQuiz(Long quizSetId, Long quizId, Long userId) {
        Quiz quiz = findQuizById(quizId);

        // 일관성 검증
        validatePathConsistency(quiz, quizSetId);
        // 권한 검증
        validateOwnership(quiz.getQuizSet(), userId);

        QuizSet quizSet = quiz.getQuizSet();
        quizRepository.delete(quiz);
        quizSet.decreaseQuizCount();
    }

    /**
     * 내부적으로 사용되는 퀴즈 세트 조회 도구입니다.
     *
     * @param quizSetId 조회할 ID
     * @return 발견된 퀴즈 세트 엔티티
     * @throws QuizSetException 엔티티가 존재하지 않을 경우
     */
    private QuizSet findQuizSetById(Long quizSetId) {
        return quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new QuizSetException(QuizSetErrorCode.QUIZ_SET_NOT_FOUND));
    }

    /**
     * 내부적으로 사용되는 퀴즈 조회 도구입니다.
     *
     * @param quizId 조회할 ID
     * @return 발견된 퀴즈 엔티티
     * @throws QuizException 엔티티가 존재하지 않을 경우
     */
    private Quiz findQuizById(Long quizId) {
        return quizRepository.findById(quizId).orElseThrow(() -> new QuizException(QuizErrorCode.QUIZ_NOT_FOUND,
                "[QuizServiceImpl] can't find quiz with id: " + quizId));
    }

    /**
     * 사용자 권한을 검증합니다.
     *
     * @param quizSet 소속 퀴즈 세트
     * @param userId  요청 사용자 ID
     * @throws AuthException 제작자가 아닐 경우
     */
    private void validateOwnership(QuizSet quizSet, Long userId) {
        if (!quizSet.getCreator().getId().equals(userId)) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL);
        }
    }

    /**
     * 요청 경로와 실제 데이터 간의 소속 관계를 검증합니다.
     *
     * @param quiz      대상 퀴즈
     * @param quizSetId 요청 경로에 포함된 퀴즈 세트 ID
     * @throws QuizException 소속 관계가 불일치할 경우
     */
    private void validatePathConsistency(Quiz quiz, Long quizSetId) {
        if (!quiz.getQuizSet().getId().equals(quizSetId)) {
            throw new QuizException(QuizErrorCode.QUIZ_NOT_FOUND,
                    "The quiz does not belong to the specified quiz set.");
        }
    }

    /**
     * 객관식 문제인 경우 선택지 4개가 모두 존재하는지 검증합니다.
     *
     * @param answerType 정답 유형
     * @param c1 선택지 1
     * @param c2 선택지 2
     * @param c3 선택지 3
     * @param c4 선택지 4
     * @throws QuizException 객관식인데 선택지가 누락된 경우
     */
    private void validateMultipleChoiceOptions(AnswerType answerType, String c1, String c2, String c3, String c4) {
        if (answerType == AnswerType.MULTIPLE_CHOICE) {
            if (c1 == null || c1.isBlank() ||
                c2 == null || c2.isBlank() ||
                c3 == null || c3.isBlank() ||
                c4 == null || c4.isBlank()) {
                throw new QuizException(QuizErrorCode.QUIZ_MULTIPLE_CHOICE_OPTIONS_REQUIRED);
            }
        }
    }
}
