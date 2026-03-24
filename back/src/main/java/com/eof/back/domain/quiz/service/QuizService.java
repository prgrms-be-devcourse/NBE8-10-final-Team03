package com.eof.back.domain.quiz.service;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import com.eof.back.domain.quiz.dto.QuizUpdateRequest;
import java.util.List;

/**
 * 퀴즈(Quiz) 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 퀴즈는 퀴즈 세트(QuizSet)의 구성 요소로, 실제 문제와 정답, 선택지 정보를 포함하는 엔티티입니다.
 * 이 인터페이스를 통해 퀴즈의 생성, 조회, 수정, 삭제 및 퀴즈 세트와의 연관 관계 관리 등의 비즈니스 로직을 정의합니다.
 * </p>
 *
 * <p><b>주요 역할:</b><br>
 * - 특정 퀴즈 세트에 속하는 새로운 퀴즈의 생성 및 추가 <br>
 * - 개별 퀴즈 정보 및 세트별 퀴즈 목록 조회 <br>
 * - 기존 퀴즈 정보의 부분 수정 (PATCH) <br>
 * - 퀴즈 삭제 및 관련 메타데이터(문제 수 등) 동기화 <br>
 * </p>
 *
 * @author MintyU
 * @since 2026-03-23
 */
public interface QuizService {

    /**
     * 특정 식별자(ID)를 가진 퀴즈 세트에 새로운 퀴즈를 생성하여 추가합니다.
     * <p>
     * 전달받은 생성 요청 정보(DTO)를 바탕으로 {@link com.eof.back.domain.quiz.entity.Quiz} 엔티티를 생성하고,
     * 지정된 퀴즈 세트와의 연관 관계를 설정하여 저장합니다. 성공 시 해당 세트의 총 문제 수가 증가합니다.
     * </p>
     *
     * @param quizSetId 퀴즈를 추가할 대상 퀴즈 세트의 식별자
     * @param request   퀴즈 생성에 필요한 정보 (문제 내용, 정답, 선택지 등)
     * @return 생성된 퀴즈의 식별자(ID)
     * @throws com.eof.back.global.exception.exceptions.QuizSetException 대상 퀴즈 세트가 존재하지 않을 경우 발생합니다.
     */
    Long createQuiz(Long quizSetId, QuizCreateRequest request);

    /**
     * 특정 식별자(ID)를 가진 퀴즈의 정보를 상세 조회합니다.
     *
     * @param quizId 조회할 퀴즈의 식별자
     * @return 퀴즈 상세 정보 DTO
     * @throws com.eof.back.global.exception.exceptions.QuizException 해당 식별자의 퀴즈가 존재하지 않을 경우 발생합니다.
     */
    QuizResponse getQuiz(Long quizId);

    /**
     * 특정 퀴즈 세트에 포함된 모든 퀴즈 목록을 조회합니다.
     *
     * @param quizSetId 퀴즈 세트의 식별자
     * @return 해당 세트에 속한 퀴즈 상세 정보 목록
     * @throws com.eof.back.global.exception.exceptions.QuizSetException 대상 퀴즈 세트가 존재하지 않을 경우 발생합니다.
     */
    List<QuizResponse> getQuizzesByQuizSetId(Long quizSetId);

    /**
     * 기존 퀴즈의 정보를 수정합니다.
     * <p>
     * 전달받은 요청 정보 중 null이 아닌 필드만 선택적으로 업데이트합니다.
     * </p>
     *
     * @param quizId  수정할 퀴즈의 식별자
     * @param request 수정할 필드 정보 (문제 내용, 정답, 선택지 등)
     * @return 수정된 퀴즈의 식별자(ID)
     * @throws com.eof.back.global.exception.exceptions.QuizException 해당 식별자의 퀴즈가 존재하지 않을 경우 발생합니다.
     */
    Long updateQuiz(Long quizId, QuizUpdateRequest request);

    /**
     * 특정 퀴즈를 삭제합니다.
     * <p>
     * 삭제 시 해당 퀴즈가 속했던 퀴즈 세트의 총 문제 수가 자동으로 감소합니다.
     * </p>
     *
     * @param quizId 삭제할 퀴즈의 식별자
     * @throws com.eof.back.global.exception.exceptions.QuizException 해당 식별자의 퀴즈가 존재하지 않을 경우 발생합니다.
     */
    void deleteQuiz(Long quizId);
}
