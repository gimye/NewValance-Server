// src/main/java/capston/new_valance/dto/req/CreateCommentRequest.java
package capston.new_valance.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
    private String content;
}
