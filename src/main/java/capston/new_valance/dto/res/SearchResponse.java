package capston.new_valance.dto.res;

import capston.new_valance.dto.NewsSimpleDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SearchResponse {
    private List<NewsSimpleDto> articles;
}
