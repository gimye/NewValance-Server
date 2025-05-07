// BannerResponseDto.java
package capston.new_valance.dto.res;

import capston.new_valance.dto.VideoVersionDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BannerResponse {
    private Long articleId;
    private String title;
    private String thumbnailUrl;
    private List<VideoVersionDto> videoVersions;
}
