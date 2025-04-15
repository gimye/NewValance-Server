// VideoListResponse.java
package capston.new_valance.dto.res;

import capston.new_valance.dto.NewsWithVideosDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VideoListResponse {
    private List<NewsWithVideosDto> news;
    private Long nextNewsId;
}
