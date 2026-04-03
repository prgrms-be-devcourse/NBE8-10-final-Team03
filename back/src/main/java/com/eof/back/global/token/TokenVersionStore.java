package com.eof.back.global.token;

import java.util.Optional;

/**
 * 토큰 버전 저장소의 동작을 정의하는 인터페이스.
 *
 * <p><b>도입 배경:</b><br>
 * JWT는 stateless 특성으로 인해 서버가 발급한 토큰을 직접 무효화할 수 없습니다.
 * 이를 해결하기 위해 사용자별 tokenVersion을 별도 저장소에서 관리합니다.
 * 발급된 JWT에 현재 tokenVersion을 포함시키고, 매 요청마다 저장소의 값과 비교하여
 * 버전이 다른 토큰은 인증 실패로 처리합니다.
 *
 * <p><b>활용 시나리오:</b><br>
 * - 로그인 시 version 증가 → 이전 기기/세션의 access token 즉시 무효화 (1계정 1세션)<br>
 * - 관리자 정지 시 version 증가 → 정지된 사용자의 토큰 즉시 무효화<br>
 * - 로그아웃/탈퇴/관리자 삭제 시 version 키 삭제 → 이후 모든 토큰 요청 차단
 *
 * <p><b>키 구조:</b><br>
 * {@code token:version:{userId}} → Long
 *
 * @author 5h6vm
 * @since 2026-04-02
 */
public interface TokenVersionStore {

    /**
     * 사용자의 tokenVersion을 1 증가시키고 새 값을 반환합니다.
     *
     * <p>호출 시점:
     * <ul>
     *   <li>로그인 성공 - 이전 세션의 access token을 즉시 무효화하여 1계정 1세션을 보장합니다.</li>
     *   <li>로그아웃 - version을 증가시켜 현재 토큰을 무효화합니다.</li>
     *   <li>관리자 정지 - 정지 처리와 동시에 해당 사용자의 현재 토큰을 무효화합니다.</li>
     * </ul>
     *
     * @param userId 사용자 ID
     * @return 증가된 새 version 값 (이 값이 새 access token의 claim에 포함됩니다)
     */
    long increment(Long userId);

    /**
     * 사용자의 현재 tokenVersion을 조회합니다.
     *
     * <p>키가 존재하지 않으면 {@link Optional#empty()}를 반환합니다.
     * 이 경우는 다음 상태 중 하나를 의미합니다:
     * <ul>
     *   <li>로그아웃하여 키가 삭제된 상태</li>
     *   <li>관리자에 의해 계정이 삭제된 상태</li>
     *   <li>한 번도 로그인한 적 없는 상태</li>
     * </ul>
     * 위 모든 경우 {@link com.eof.back.global.jwt.JwtAuthenticationFilter}에서 인증 실패로 처리합니다.
     *
     * @param userId 사용자 ID
     * @return 저장된 version 값, 키가 없으면 {@link Optional#empty()}
     */
    Optional<Long> findByUserId(Long userId);

    /**
     * 사용자의 tokenVersion 키를 삭제합니다.
     *
     * <p>호출 시점:
     * <ul>
     *   <li>회원 탈퇴 - soft delete 처리 후 잔여 토큰을 즉시 무효화합니다.</li>
     *   <li>관리자 삭제 - 영구 삭제(soft) 처리 시 키를 제거합니다.</li>
     * </ul>
     *
     * @param userId 사용자 ID
     */
    void delete(Long userId);
}
