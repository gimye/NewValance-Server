package capston.new_valance.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NewsStandResponseDto {
    private Long categoryId;
    private String categoryName;
    private List<NewsSimpleDto> newsList;
}
