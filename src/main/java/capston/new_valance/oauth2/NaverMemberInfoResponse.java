package capston.new_valance.oauth2;

import java.util.Map;

public class NaverMemberInfoResponse implements OAuth2Response {
    private final Map<String, Object> attributes;

    public NaverMemberInfoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("nickname");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("profile_image");
    }
}
