package capston.new_valance.dto.req;

import lombok.Getter;

@Getter
public class VideoMetadataRequest {
    private String title;
    private Long categoryId;
    private String originalUrl;
    private String publishedAt;
    private String createdAt;
    private String easyVersionUrl;
    private String normalVersionUrl;
    private String thumbnailUrl;
    private String tags;
}
