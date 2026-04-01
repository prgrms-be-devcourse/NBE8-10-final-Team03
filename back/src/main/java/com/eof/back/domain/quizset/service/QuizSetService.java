package com.eof.back.domain.quizset.service;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import com.eof.back.domain.quizset.dto.QuizSetUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

/**
 * 퀴즈 세트(QuizSet) 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 퀴즈 세트는 퀴즈들을 묶어 관리하는 최상위 엔티티로, 사용자들의 학습 및 게임 플레이의 중심이 됩니다.
 * 이 인터페이스를 통해 퀴즈 세트의 생성, 조회, 수정, 삭제 등의 비즈니스 로직에 대한 규격을 정의합니다.
 *
 * <p><b>주요 역할:</b><br>
 * - 새로운 퀴즈 세트의 생성 및 초기 설정 <br>
 * - 퀴즈 세트 정보의 조회 및 필터링 <br>
 * - 퀴즈 세트의 소유권 및 권한 관리 <br>
 * - 퀴즈 세트와 관련된 부가 기능(통계, 추천 등) 제공 <br>
 *
 * @author MintyU
 * @since 2026-03-19
 */
public interface QuizSetService {

    /**
     * 새로운 퀴즈 세트를 생성하고 영속성 컨텍스트에 저장합니다.
     *
     * @param request 퀴즈 세트 생성 요청 정보
     * @param userId  제작자의 식별자(ID)
     * @return 생성된 퀴즈 세트의 식별자(ID)
     */
    Long createQuizSet(QuizSetCreateRequest request, Long userId);

    /**
     * 특정 식별자(ID)를 가진 퀴즈 세트의 상세 정보를 조회합니다.
     * <p>
     * <b>보안 규칙:</b><br>
     * 퀴즈의 정답 정보를 포함하고 있으므로, 해당 퀴즈 세트를 생성한 **작성자 본인** 또는 **관리자(ADMIN)**만 조회가 가능합니다.
     * </p>
     *
     * @param id     조회할 퀴즈 세트의 식별자
     * @param userId 요청을 보낸 사용자의 식별자 (권한 확인용)
     * @return 퀴즈 목록을 포함한 퀴즈 세트 상세 정보
     * @throws com.eof.back.global.exception.exceptions.QuizSetException 권한이 없거나 존재하지 않을 경우 발생합니다.
     */
    QuizSetResponse getQuizSet(Long id, Long userId);

    /**
     * 시스템에 등록된 모든 퀴즈 세트 목록을 조회합니다.
     * 요약 정보(제목, 제작자, 총 문제 수 등)만 반환합니다.
     *
     * @param pageable 페이징 정보
     * @return 등록된 모든 퀴즈 세트의 요약 정보 목록
     */
    Slice<QuizSetListResponse> getAllQuizSets(Pageable pageable);

    /**
     * 기존 퀴즈 세트의 정보를 수정합니다.
     *
     * @param id      수정할 퀴즈 세트의 식별자
     * @param request 수정할 필드 정보
     * @param userId  요청을 보낸 사용자의 식별자 (권한 확인용)
     * @return 수정된 퀴즈 세트의 식별자(ID)
     */
    Long updateQuizSet(Long id, QuizSetUpdateRequest request, Long userId);

    /**
     * 특정 퀴즈 세트를 삭제합니다.
     *
     * @param id     삭제할 퀴즈 세트의 식별자
     * @param userId 요청을 보낸 사용자의 식별자 (권한 확인용)
     */
    void deleteQuizSet(Long id, Long userId);
}
