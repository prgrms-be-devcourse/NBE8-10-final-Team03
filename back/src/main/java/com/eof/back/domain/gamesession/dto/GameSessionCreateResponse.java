package com.eof.back.domain.gamesession.dto;

import com.eof.back.domain.gamesession.entity.GameSession;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 클라이언트에게 생성된 게임 세션(방)의 상세 정보를 전달하는 응답용 DTO입니다.
 * <p>
 * 데이터베이스에 성공적으로 저장된 {@code GameSession} 엔티티 객체를 입력받아,
 * 클라이언트가 필요로 하는 응답 스펙에 맞춰 데이터를 매핑하고 불변 객체로 반환합니다.
 * 정적 팩토리 메서드({@code from})를 통해 엔티티와 DTO 간의 변환 로직을 캡슐화합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 자바의 {@code record} 타입이므로 내부적으로 {@code java.lang.Record}를 암묵적으로 상속받습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 필드로 선언된 모든 컴포넌트를 매개변수로 받는 표준 생성자(Canonical Constructor)가 자동 생성됩니다.<br>
 *
 * <p><b>빈 관리:</b><br>
 * 스프링 빈(Bean)으로 관리되지 않으며, API 응답 시점에 생성되어 JSON 직렬화 후 소멸됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Lombok: {@code @Builder} 어노테이션을 사용하여 정적 팩토리 메서드 내부에서 가독성 높은 객체 생성을 지원합니다.
 *
 * @author 유재원
 * @see com.eof.back.domain.gamesession.entity.GameSession
 * @since 2026-03-18
 */

@Builder
public record GameSessionCreateResponse(
        Long roomId,
        String roomName,
        Long hostUserId,
        Long quizSetId,
        Integer maxPlayers,
        String status,
        LocalDateTime createdAt,
        Integer maxQuizzes
) {

    /**
     * 엔티티(GameSession)를 받아서 응답용 DTO로 변환하는 정적 팩토리 메서드입니다.
     */
    public static GameSessionCreateResponse from(GameSession gameSession) {
        return GameSessionCreateResponse.builder()
                .roomId(gameSession.getId())
                .roomName(gameSession.getRoomName())
                .hostUserId(gameSession.getHost().getId())
                .quizSetId(gameSession.getQuizSet().getId())
                .maxPlayers(gameSession.getMaxPlayers())
                .status(gameSession.getStatus().name())
                .createdAt(gameSession.getCreatedAt())
                .maxQuizzes(gameSession.getMaxQuizzes())
                .build();
    }
}
