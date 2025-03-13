package capston.new_valance.oauth2;

import capston.new_valance.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public CustomOAuth2User(User user, Map<String, Object> attributes, String nameAttributeKey) {
        this.user = user;
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한이 필요한 경우 Collection으로 리턴, 여기선 빈 리스트로 처리
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        // nameAttributeKey (예, "id")에 해당하는 값을 문자열로 반환
        return String.valueOf(attributes.get(nameAttributeKey));
    }
}
