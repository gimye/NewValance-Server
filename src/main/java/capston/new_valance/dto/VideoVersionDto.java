package capston.new_valance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoVersionDto {
    private Long videoId;
    private String videoUrl;
    private String thumbnailUrl;
}
