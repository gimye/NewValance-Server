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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String providerId = userRequest.getClientRegistration().getRegistrationId().toLowerCase();

        try {
            return switch (providerId) {
                case "kakao" -> processKakaoUser(oAuth2User);
                case "naver" -> processNaverUser(oAuth2User);
                case "google" -> processGoogleUser(oAuth2User);
                default -> throw createOAuthException("unsupported_provider", "지원하지 않는 공급자: " + providerId);
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
        checkExistingEmail(response.getEmail(), LoginProvider.google);

        AtomicBoolean isNew = new AtomicBoolean(false);
        User user = userRepository.findByEmailAndLoginProvider(response.getEmail(), LoginProvider.google)
                .map(existingUser -> updateExistingUser(existingUser, response))
                .orElseGet(() -> {
                    isNew.set(true);
                    return userRepository.save(createNewUser(response, LoginProvider.google));
                });

        return new CustomOAuth2User(user, oAuth2User.getAttributes(), "sub", isNew.get());
    }

    private OAuth2User processKakaoUser(OAuth2User oAuth2User) {
        KakaoMemberInfoResponse response = new KakaoMemberInfoResponse(oAuth2User.getAttributes());
        validateEmail(response.getEmail(), "Kakao");
        checkExistingEmail(response.getEmail(), LoginProvider.kakao);

        AtomicBoolean isNew = new AtomicBoolean(false);
        User user = userRepository.findByEmailAndLoginProvider(response.getEmail(), LoginProvider.kakao)
                .map(existingUser -> updateExistingUser(existingUser, response))
                .orElseGet(() -> {
                    isNew.set(true);
                    return userRepository.save(createNewUser(response, LoginProvider.kakao));
                });

        return new CustomOAuth2User(user, oAuth2User.getAttributes(), "id", isNew.get());
    }

    private OAuth2User processNaverUser(OAuth2User oAuth2User) {
        Map<String, Object> responseMap = (Map<String, Object>) oAuth2User.getAttributes().get("response");
        NaverMemberInfoResponse response = new NaverMemberInfoResponse(responseMap);
        validateEmail(response.getEmail(), "Naver");
        checkExistingEmail(response.getEmail(), LoginProvider.naver);

        AtomicBoolean isNew = new AtomicBoolean(false);
        User user = userRepository.findByEmailAndLoginProvider(response.getEmail(), LoginProvider.naver)
                .map(existingUser -> updateExistingUser(existingUser, response))
                .orElseGet(() -> {
                    isNew.set(true);
                    return userRepository.save(createNewUser(response, LoginProvider.naver));
                });

        return new CustomOAuth2User(user, responseMap, "id", isNew.get());
    }

    private User createNewUser(OAuth2Response response, LoginProvider provider) {
        return User.builder(response.getEmail(), provider)
                .username(response.getNickname())
                .profilePictureUrl(response.getProfileImageUrl())
                .createdAt(LocalDateTime.now())
                .build();
    }

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
        boolean needsUpdate = false;

        if (user.getUsername() == null && response.getNickname() != null) {
            user = user.toBuilder().username(response.getNickname()).build();
            needsUpdate = true;
        }

        if (user.getProfilePictureUrl() == null && response.getProfileImageUrl() != null) {
            user = user.toBuilder().profilePictureUrl(response.getProfileImageUrl()).build();
            needsUpdate = true;
        }

        return needsUpdate ? userRepository.save(user) : user;
    }

    private OAuth2AuthenticationException createOAuthException(String errorCode, String message) {
        return new OAuth2AuthenticationException(new OAuth2Error(errorCode, message, null));
    }
}
