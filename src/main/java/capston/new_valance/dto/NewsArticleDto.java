package capston.new_valance.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NewsArticleDto {
    private Long articleId;
    private String title;
    private Long categoryId;
    private String originalUrl;
    private List<VideoVersionDto> videoVersions;
}
