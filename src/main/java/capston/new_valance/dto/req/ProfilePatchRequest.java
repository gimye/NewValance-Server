package capston.new_valance.dto.req;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class ProfilePatchRequest {
    private final String username;
    private final MultipartFile profileImage;
}
