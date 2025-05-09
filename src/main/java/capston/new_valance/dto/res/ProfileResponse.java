package capston.new_valance.dto.res;

import capston.new_valance.dto.PreferredKeywordsDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ProfileResponse {
    private String username;
    private String profileImgUrl;
    private Long todayViews;
    private Long totalViews;
    private List<PreferredKeywordsDto> preferredKeywords;

}
