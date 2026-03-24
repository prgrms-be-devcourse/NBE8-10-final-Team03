package com.eof.back.domain.quiz.service;

import com.eof.back.domain.quiz.dto.QuizCreateRequest;
import com.eof.back.domain.quiz.dto.QuizResponse;
import java.util.List;

/**
 * 퀴즈(Quiz) 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 퀴즈는 퀴즈 세트(QuizSet)의 구성 요소로, 실제 문제와 정답, 선택지 정보를 포함하는 엔티티입니다.
 * 이 인터페이스를 통해 퀴즈의 생성 및 퀴즈 세트와의 연관 관계 관리 등의 비즈니스 로직을 정의합니다.
 *
 * <p><b>주요 역할:</b><br>
 * - 특정 퀴즈 세트에 속하는 새로운 퀴즈의 생성 <br>
 * - 퀴즈 내용 및 선택지 정보의 유효성 검증 <br>
 * - 퀴즈 세트 내의 퀴즈 수 제한 관리 <br>
 *
 * <p><b>예외 상황:</b><br>
 * - 대상 퀴즈 세트가 존재하지 않는 경우 예외가 발생할 수 있습니다. <br>
 * - 퀴즈 세트에 더 이상 퀴즈를 추가할 수 없는 경우(최대 개수 초과) 예외가 발생할 수 있습니다. <br>
 *
 * @author MintyU
 * @since 2026-03-23
 */
public interface QuizService {

    /**
     * 특정 식별자(ID)를 가진 퀴즈 세트에 새로운 퀴즈를 생성하여 추가합니다.
     * <p>
     * 전달받은 생성 요청 정보(DTO)를 바탕으로 {@link com.eof.back.domain.quiz.entity.Quiz} 엔티티를 생성하고,
     * 지정된 퀴즈 세트와의 연관 관계를 설정하여 영속성 컨텍스트에 저장합니다.
     *
     * <p><b>비즈니스 규칙:</b><br>
     * 1. 퀴즈 내용(content)과 정답(answer)은 필수이며 공백일 수 없습니다. <br>
     * 2. 선택지(choice1~4)는 해당 퀴즈의 보기를 구성합니다. <br>
     *
     * @param quizSetId 퀴즈를 추가할 대상 퀴즈 세트의 식별자
     * @param request 퀴즈 생성에 필요한 정보 (문제 내용, 정답, 선택지 등)
     * @return 생성된 퀴즈의 식별자(ID)
     * @throws com.eof.back.global.exception.exceptions.QuizSetException 대상 퀴즈 세트가 존재하지 않을 경우 발생합니다.
     * @throws com.eof.back.global.exception.exceptions.QuizException 퀴즈 최대 개수를 초과하거나 유효하지 않은 요청인 경우 발생합니다.
     */
    Long createQuiz(Long quizSetId, QuizCreateRequest request);

    /**
     * 특정 식별자(ID)를 가진 퀴즈의 정보를 조회합니다.
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
     * @return 퀴즈 상세 정보 목록
     * @throws com.eof.back.global.exception.exceptions.QuizSetException 대상 퀴즈 세트가 존재하지 않을 경우 발생합니다.
     */
    List<QuizResponse> getQuizzesByQuizSetId(Long quizSetId);
}
