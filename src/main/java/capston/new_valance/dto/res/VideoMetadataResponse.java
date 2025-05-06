package capston.new_valance.dto.res;

import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.TagDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VideoMetadataResponse {
    private Long articleId;
    private String title;
    private Long categoryId;
    private String originalUrl;
    private String publishedAt;
    private String createdAt;
    private List<VideoVersionDto> videoVersions;
    private List<TagDto> tags;
}