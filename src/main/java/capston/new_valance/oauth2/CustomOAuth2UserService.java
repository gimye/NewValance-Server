package capston.new_valance.oauth2;

import capston.new_valance.model.LoginProvider;
import capston.new_valance.model.User;
import capston.new_valance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId().toLowerCase();

        try {
            return switch (provider) {
                case "kakao" -> processKakaoUser(oAuth2User);
                case "naver" -> processNaverUser(oAuth2User);
                case "google" -> processGoogleUser(oAuth2User); // 구글 분기 추가
                default -> throw createOAuthException("unsupported_provider", "지원하지 않는 공급자: " + provider);
            };
        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw createOAuthException("internal_error", "OAuth2 처리 실패: " + ex.getMessage());
        }
    }

    private OAuth2User processGoogleUser(OAuth2User oAuth2User) {
        GoogleMemberInfoResponse response = new GoogleMemberInfoResponse(oAuth2User.getAttributes());
        validateEmail(response.getEmail(), "Google");
        // LoginProvider enum에 google 값이 있어야 함
        checkExistingEmail(response.getEmail(), LoginProvider.google);

        User user = userRepository.findByEmailAndLoginProvider(response.getEmail(), LoginProvider.google)
                .map(existingUser -> updateExistingUser(existingUser, response))
                .orElseGet(() -> createNewUser(response, LoginProvider.google));

        // 구글의 경우 user info 응답에서 사용자 식별자는 "sub" 필드로 제공됨
        return new CustomOAuth2User(user, oAuth2User.getAttributes(), "sub");
    }


    private OAuth2User processKakaoUser(OAuth2User oAuth2User) {
        KakaoMemberInfoResponse response = new KakaoMemberInfoResponse(oAuth2User.getAttributes());
        validateEmail(response.getEmail(), "Kakao");
        // 로그인 공급자를 함께 확인하여, 동일 공급자인 경우에만 업데이트를 진행
        checkExistingEmail(response.getEmail(), LoginProvider.kakao);

        User user = userRepository.findByEmailAndLoginProvider(response.getEmail(), LoginProvider.kakao)
                .map(existingUser -> updateExistingUser(existingUser, response))
                .orElseGet(() -> createNewUser(response, LoginProvider.kakao));

        return new CustomOAuth2User(user, oAuth2User.getAttributes(), "id");
    }

    private OAuth2User processNaverUser(OAuth2User oAuth2User) {
        Map<String, Object> responseMap = (Map<String, Object>) oAuth2User.getAttributes().get("response");
        NaverMemberInfoResponse response = new NaverMemberInfoResponse(responseMap);
        validateEmail(response.getEmail(), "Naver");
        // 로그인 공급자를 함께 확인하여, 동일 공급자인 경우에만 업데이트를 진행
        checkExistingEmail(response.getEmail(), LoginProvider.naver);

        User user = userRepository.findByEmailAndLoginProvider(response.getEmail(), LoginProvider.naver)
                .map(existingUser -> updateExistingUser(existingUser, response))
                .orElseGet(() -> createNewUser(response, LoginProvider.naver));

        return new CustomOAuth2User(user, responseMap, "id");
    }

    // 이메일 중복 확인 시, 로그인 공급자까지 고려하도록 수정
    private void checkExistingEmail(String email, LoginProvider provider) {
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getLoginProvider().equals(provider)) {
                throw createOAuthException(
                        "duplicate_email",
                        "이미 가입된 이메일이 있습니다. 기존 로그인 방식을 사용해주세요."
                );
            }
        });
    }

    private void validateEmail(String email, String provider) {
        if (email == null || email.isBlank()) {
            throw createOAuthException("missing_email", provider + " 계정에서 이메일을 찾을 수 없습니다.");
        }
    }

    private User updateExistingUser(User user, OAuth2Response response) {
        if (user.getUsername() == null) {
            user.setUsername(response.getNickname());
        }
        if (user.getProfilePictureUrl() == null) {
            user.setProfilePictureUrl(response.getProfileImageUrl());
        }
        return userRepository.save(user);
    }

    private User createNewUser(OAuth2Response response, LoginProvider provider) {
        User newUser = User.createSocialUser(response.getEmail(), provider);
        newUser.setUsername(response.getNickname());
        newUser.setProfilePictureUrl(response.getProfileImageUrl());
        return userRepository.save(newUser);
    }

    // 예외 생성 유틸리티 메서드
    private OAuth2AuthenticationException createOAuthException(String errorCode, String message) {
        OAuth2Error error = new OAuth2Error(
                errorCode,
                message,
                null
        );
        return new OAuth2AuthenticationException(error);
    }
}
