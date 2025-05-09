// src/main/java/capston/new_valance/dto/CommentDto.java
package capston.new_valance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDto {
    private Long commentId;
    private String username;
    private String profileImgUrl;
    private String content;
    private boolean isMine;
}
