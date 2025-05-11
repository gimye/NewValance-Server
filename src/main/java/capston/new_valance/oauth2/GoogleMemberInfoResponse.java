package capston.new_valance.oauth2;

import java.util.Map;

public class GoogleMemberInfoResponse implements OAuth2Response {
    private final Map<String, Object> attributes;

    public GoogleMemberInfoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {

        return (String) attributes.get("name");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("picture");
    }
}
