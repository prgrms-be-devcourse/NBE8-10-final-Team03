package com.eof.back.domain.quizset.service;

import com.eof.back.domain.quizset.dto.QuizSetCreateRequest;
import com.eof.back.domain.quizset.dto.QuizSetListResponse;
import com.eof.back.domain.quizset.dto.QuizSetResponse;
import java.util.List;

/**
 * 퀴즈 세트(QuizSet) 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 * <p>
 * 퀴즈 세트는 퀴즈들을 묶어 관리하는 최상위 엔티티로, 사용자들의 학습 및 게임 플레이의 중심이 됩니다.
 * 이 인터페이스를 통해 퀴즈 세트의 생성, 조회, 수정, 삭제 등의 비즈니스 로직에 대한 규격을 정의합니다.
 *
 * <p><b>주요 역할:</b><br>
 * - 새로운 퀴즈 세트의 생성 및 초기 설정 <br>
 * - 퀴즈 세트 정보의 조회 및 필터링 <br>
 * - 퀴즈 세트의 소유권 및 권한 관리 (예정) <br>
 * - 퀴즈 세트와 관련된 부가 기능(통계, 추천 등) 제공 (예정) <br>
 *
 * <p><b>예외 상황:</b><br>
 * - 생성 요청 정보가 누락되거나 유효하지 않은 경우 예외가 발생할 수 있습니다. <br>
 * - 제작자 정보를 찾을 수 없는 경우 예외가 발생할 수 있습니다. <br>
 *
 * @author MintyU
 * @since 2026-03-19
 */
public interface QuizSetService {

    /**
     * 새로운 퀴즈 세트를 생성하고 영속성 컨텍스트에 저장합니다.
     * <p>
     * 전달받은 생성 요청 정보(DTO)를 바탕으로 {@link com.eof.back.domain.quizset.entity.QuizSet} 엔티티를 생성합니다.
     * 생성 시 현재 로그인된 사용자를 제작자로 설정하며, 제목, 설명, 총 문제 수 등을 초기화합니다.
     *
     * <p><b>비즈니스 규칙:</b><br>
     * 1. 제목과 설명은 필수이며 공백일 수 없습니다. <br>
     * 2. 총 문제 수는 1개 이상이어야 합니다. <br>
     * 3. 제작자는 시스템에 등록된 유효한 사용자여야 합니다. <br>
     *
     * @param request 퀴즈 세트 생성에 필요한 정보 (제목, 설명, 총 문제 수 등)가 담긴 객체
     * @param userId 제작자의 식별자(ID)
     * @return 생성된 퀴즈 세트의 식별자(ID)
     * @throws RuntimeException (임시) 제작자 정보를 찾을 수 없을 경우 발생합니다.
     */
    Long createQuizSet(QuizSetCreateRequest request, Long userId);

    /**
     * 특정 식별자(ID)를 가진 퀴즈 세트의 상세 정보를 조회합니다.
     * <p>
     * 이 메서드는 퀴즈 세트의 기본 정보뿐만 아니라 소속된 모든 퀴즈 목록을 함께 반환합니다.
     *
     * @param id 조회할 퀴즈 세트의 식별자
     * @return 퀴즈 목록을 포함한 퀴즈 세트 상세 정보
     * @throws com.eof.back.global.exception.exceptions.QuizSetException 해당 ID의 퀴즈 세트가 존재하지 않을 경우 발생합니다.
     */
    QuizSetResponse getQuizSet(Long id);

    /**
     * 시스템에 등록된 모든 퀴즈 세트 목록을 조회합니다.
     * <p>
     * 목록 조회 시에는 데이터 전송 효율을 위해 개별 퀴즈들의 상세 목록은 제외하고,
     * 각 퀴즈 세트의 요약 정보(제목, 제작자, 총 문제 수 등)만 반환합니다.
     *
     * @return 등록된 모든 퀴즈 세트의 요약 정보 목록
     */
    List<QuizSetListResponse> getAllQuizSets();
}
