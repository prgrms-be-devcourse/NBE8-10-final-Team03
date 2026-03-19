package com.eof.back.domain.gamesession.controller;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;
import com.eof.back.domain.gamesession.service.GameSessionService;
import com.eof.back.domain.user.entity.User;
import com.eof.back.global.response.CommonResponse;
import com.eof.back.global.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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

    @PostMapping
    public ResponseEntity<Response<GameSessionCreateResponse>> createPost(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody GameSessionCreateRequest request
    ) {
        GameSessionCreateResponse response = gameSessionService.createGameSession(user.getId(), request);

        return ResponseEntity.created(URI.create("/api/v1/rooms/" + response.gameSessionId()))
                .body(CommonResponse.success(response, "방 생성이 완료되었습니다."));
    }
}
