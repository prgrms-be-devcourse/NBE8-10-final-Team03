package com.eof.back.domain.gamesession.controller;

import com.eof.back.domain.gamesession.dto.GameSessionCreateRequest;
import com.eof.back.domain.gamesession.dto.GameSessionCreateResponse;
import com.eof.back.domain.gamesession.service.GameSessionService;
import com.eof.back.domain.user.entity.User;
import com.eof.back.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author 유재원
 * @see
 * @since 2026-03-18
 */

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class GameSessionController {
    private final GameSessionService gameSessionService;

    @PostMapping
    public ResponseEntity<CommonResponse<GameSessionCreateResponse>> createPost(
            User user,
            @Valid @RequestBody GameSessionCreateRequest request
    ) {
        GameSessionCreateResponse response = gameSessionService.createGameSession(user.getId(), request);

        return ResponseEntity.created(URI.create("/api/v1/rooms/" + response.gameSessionId()))
                .body(CommonResponse.success(response, "방 생성이 완료되었습니다."));
    }
}
