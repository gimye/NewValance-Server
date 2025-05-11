package capston.new_valance.dto.req;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class ProfilePatchRequest {
    private String username;
    private MultipartFile profileImage;
}