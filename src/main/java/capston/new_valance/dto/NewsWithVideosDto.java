// NewsWithVideosDto.java
package capston.new_valance.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NewsWithVideosDto {
    private Long newsId;
    private String title;
    private String originalUrl;
    private String thumbnailUrl; // 썸네일 URL 포함
    private List<VideoVersionDto> videoVersions;
}
