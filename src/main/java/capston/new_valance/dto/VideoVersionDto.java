// VideoVersionDto.java
package capston.new_valance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoVersionDto {
    private String versionName;
    private String videoUrl;
}
