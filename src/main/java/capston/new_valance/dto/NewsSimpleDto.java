package capston.new_valance.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewsSimpleDto {
    private Long articleId;
    private String title;
    private String thumbnailUrl;
}
