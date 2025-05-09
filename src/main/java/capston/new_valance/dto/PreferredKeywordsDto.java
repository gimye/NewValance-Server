package capston.new_valance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreferredKeywordsDto {
    private String keyword;
    private Long weight;
}
