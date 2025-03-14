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
        // 구글의 기본 응답에서는 "name" 필드에 전체 이름이 포함되어 있습니다.
        // 필요에 따라 "given_name" 등을 사용할 수 있습니다.
        return (String) attributes.get("name");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("picture");
    }
}
