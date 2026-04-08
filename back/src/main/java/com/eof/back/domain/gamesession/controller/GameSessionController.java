package com.eof.back.domain.gamesession.controller;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;
import com.eof.back.domain.gamesession.dto.GameSessionJoinResponse;
import com.eof.back.domain.gamesession.dto.GameSessionListResponse;
import com.eof.back.domain.gamesession.dto.GameSessionUpdateRequest;
import com.eof.back.domain.gamesession.service.GameSessionService;
import com.eof.back.global.jwt.UserPrincipal;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * 게임 세션(방) 생성 및 관련 API 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 클라이언트로부터 게임 방 생성 요청(POST)을 받아 {@code GameSessionService}로 비즈니스 로직 처리를 위임합니다.
 * 처리 완료 후, 생성된 리소스의 위치(Location URI)와 성공 상태를 포함한 표준 응답 객체를 클라이언트에게 반환합니다.
 *
 *
 * <p><b>주요 생성자:</b><br>
 * Lombok의 {@code @RequiredArgsConstructor}를 통해 다음 의존성을 주입받는 생성자가 자동 생성됩니다.<br>
 * - {@code GameSessionController(GameSessionService gameSessionService)}: 게임 세션 비즈니스 로직 처리를 위한 서비스 객체 주입 <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @RestController} 어노테이션이 선언되어 있어 Spring ApplicationContext에 의해 싱글톤 빈(Bean)으로 자동 등록 및 관리됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * - Spring Security: {@code @AuthenticationPrincipal}을 사용하여 현재 인증된 사용자(User) 정보를 파라미터로 바인딩합니다.<br>
 * - Spring Validation: {@code @Valid} 어노테이션을 사용하여 요청 본문({@code GameSessionCreateRequest})의 데이터 유효성을 검증합니다.
 *
 * @author 유재원
 * @since 2026-03-18
 */

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class GameSessionController {
    private final GameSessionService gameSessionService;

    /**
     * 게임 세션을 만듭니다.
     * POST /api/v1/rooms
     *
     * @param userPrincipal 호스트 유저 방장
     * @param request       GameSessionCreateRequest
     * @return
     */
    @PostMapping
    public ResponseEntity<Response<GameSessionCreateResponse>> createGameSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GameSessionCreateRequest request
    ) {
        GameSessionCreateResponse response = gameSessionService.createGameSession(userPrincipal.id(), request);

        return ResponseEntity.created(URI.create("/api/v1/rooms/" + response.gameSessionId()))
                .body(CommonResponse.success(response, "방 생성이 완료되었습니다."));
    }

    /**
     * 전체 게임세션들을 리턴받습니다.
     * GET /api/v1/rooms
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<Response<List<GameSessionListResponse>>> getAllGameSession() {
        List<GameSessionListResponse> response = gameSessionService.getAllGameSessions();

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 방 이름으로 검색하여 그에 해당하는 게임세션들을 리턴받습니다.
     * GET /api/v1/rooms/search?roomName=방제목
     *
     * @param roomName 방 제목
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<Response<List<GameSessionListResponse>>> searchGameSessions(
            @RequestParam("roomName") String roomName
    ) {
        List<GameSessionListResponse> response = gameSessionService.getGameSessionByRoomName(roomName);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 게임세션을 삭제합니다.
     * DELETE /api/v1/rooms/{gameSession_Id}
     *
     * @param userPrincipal 해당 게임세션을 삭제요청 보낸 유저
     * @param gameSessionId 해당 게임세션의 아이디
     * @return
     */

    @DeleteMapping("/{gameSessionId}")
    public ResponseEntity<Void> deleteGameSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long gameSessionId
    ) {
        gameSessionService.deleteGameSession(userPrincipal.id(), gameSessionId);

        // 204 No Content 상태 코드를 반환
        return ResponseEntity.noContent().build();
    }

    /**
     * 게임 세션(방)에 입장합니다.
     *
     * @param userPrincipal 현재 로그인한 유저의 인증 정보 (ID, Username)
     * @param gameSessionId 입장하려는 게임 세션의 ID
     * @return 입장 완료된 방의 최신 상태 및 참가자 목록 DTO
     */
    @PostMapping("/{gameSessionId}/join")
    public ResponseEntity<Response<GameSessionJoinResponse>> joinGameSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long gameSessionId
    ) {
        GameSessionJoinResponse response = gameSessionService.joinRoom(userPrincipal.id(), gameSessionId);

        return ResponseEntity.ok(CommonResponse.success(response, "방에 입장하셨습니다."));
    }

    /**
     * 게임 세션(방)에서 퇴장합니다.
     *
     * @param userPrincipal 현재 로그인한 유저의 인증 정보
     * @param gameSessionId 퇴장하려는 게임 세션의 ID
     */
    @DeleteMapping("/{gameSessionId}/leave")
    public ResponseEntity<Void> leaveGameSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long gameSessionId
    ) {
        // 서비스의 퇴장 로직 호출
        gameSessionService.leaveRoom(userPrincipal.id(), gameSessionId);

        // 퇴장 성공 시 204 No Content 반환
        return ResponseEntity.noContent().build();
    }

    /**
     * 게임 세션(방)의 설정을 수정합니다.
     *
     * @param userPrincipal 호스트 유저 (방장)
     * @param gameSessionId 수정하려는 게임 세션의 ID
     * @param request       수정할 설정 정보 DTO
     * @return 수정된 방의 최신 상태 DTO
     */
    @PatchMapping("/{gameSessionId}")
    public ResponseEntity<Response<GameSessionJoinResponse>> updateGameSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long gameSessionId,
            @Valid @RequestBody GameSessionUpdateRequest request
    ) {
        GameSessionJoinResponse response = gameSessionService.updateGameSession(userPrincipal.id(), gameSessionId, request);

        return ResponseEntity.ok(CommonResponse.success(response, "방 설정이 수정되었습니다."));
    }
}
