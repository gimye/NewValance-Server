package capston.new_valance.dto.res;

import capston.new_valance.dto.NewsSimpleDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NewsStandResponse {
    private Long categoryId;
    private String categoryName;
    private List<NewsSimpleDto> newsList;
}
