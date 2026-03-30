package com.eof.back.domain.auth.oauth2;

import com.eof.back.domain.user.user.entity.AuthProvider;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 로그인 사용자 정보를 처리하는 OAuth2 유저 서비스입니다.
 *
 * <p>Spring Security의 {@link DefaultOAuth2UserService}를 상속하여,
 * 소셜 제공자로부터 사용자 정보를 받은 뒤 DB에서 기존 유저를 조회하거나
 * 신규 유저를 생성하는 비즈니스 로직을 수행합니다.</p>
 *
 * <p><b>처리 흐름:</b><br>
 * 1. 부모 클래스의 {@code loadUser()}로 소셜 제공자에서 사용자 정보 수신<br>
 * 2. {@link OAuthAttributes}로 제공자별 응답 파싱<br>
 * 3. providerId로 기존 유저 조회 → 있으면 반환, 없으면 신규 생성
 *
 * @author 5h6vm
 * @since 2026-03-30
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * 소셜 제공자로부터 사용자 정보를 받아 DB 유저와 연결합니다.
     *
     * @param userRequest 소셜 제공자의 액세스 토큰 및 클라이언트 정보
     * @return Spring Security가 인증 객체 생성에 사용할 OAuth2User
     * @throws OAuth2AuthenticationException 소셜 제공자 통신 실패 시
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. 부모 클래스로 소셜 제공자에서 사용자 정보 수신
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 제공자 식별자 (application.yml registration 키: "google", "kakao")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 제공자별 응답을 통일된 OAuthAttributes로 파싱
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());

        // 4. 기존 유저 조회 또는 신규 생성
        findOrCreateUser(attributes);

        return oAuth2User;
    }

    /**
     * providerId로 기존 유저를 조회하고, 없으면 신규 유저를 생성합니다.
     *
     * <p>소셜 닉네임이 DB에 이미 존재하면 suffix를 붙여 중복을 방지합니다.
     * 예: "홍길동" 중복 시 "홍길동_1", "홍길동_2" 순으로 시도합니다.
     *
     * @param attributes 파싱된 소셜 사용자 정보
     */
    private void findOrCreateUser(OAuthAttributes attributes) {
        userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .orElseGet(() -> createUser(attributes));
    }

    /**
     * 소셜 로그인 신규 유저를 생성하고 저장합니다.
     *
     * @param attributes 파싱된 소셜 사용자 정보
     * @return 저장된 User 엔티티
     */
    private User createUser(OAuthAttributes attributes) {
        String nickname = generateUniqueNickname(attributes.getNickname());
        User user = User.ofSocial(attributes.getEmail(), nickname, attributes.getProvider(), attributes.getProviderId());
        return userRepository.save(user);
    }

    /**
     * 닉네임 중복 시 suffix를 붙여 고유한 닉네임을 생성합니다.
     *
     * <p>예: "홍길동"이 이미 존재하면 "홍길동_1", "홍길동_1"도 존재하면 "홍길동_2" 순으로 시도합니다.
     *
     * @param nickname 소셜 제공자로부터 받은 원본 닉네임
     * @return DB에서 사용 가능한 고유 닉네임
     */
    private String generateUniqueNickname(String nickname) {
        // 한 번의 쿼리로 관련 닉네임 전부 조회 후 메모리에서 처리
        Set<String> existingNicknames = userRepository.findNicknamesStartingWith(nickname);

        if (!existingNicknames.contains(nickname)) {
            return nickname;
        }

        int suffix = 1;
        String candidate;
        do {
            candidate = nickname + "_" + suffix++;
        } while (existingNicknames.contains(candidate));

        return candidate;
    }
}
