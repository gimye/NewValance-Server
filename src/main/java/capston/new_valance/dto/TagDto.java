package capston.new_valance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagDto {
    private Long tagId;
    private String tagName;
}
