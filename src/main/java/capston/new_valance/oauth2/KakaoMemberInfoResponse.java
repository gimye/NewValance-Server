package capston.new_valance.oauth2;

import java.util.Map;

public class KakaoMemberInfoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;

    public KakaoMemberInfoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    @Override
    public String getNickname() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            return profile != null ? (String) profile.get("nickname") : null;
        }
        return null;
    }

    @Override
    public String getProfileImageUrl() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            return profile != null ? (String) profile.get("profile_image_url") : null;
        }
        return null;
    }
}
