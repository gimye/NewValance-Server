package capston.new_valance.dto.res;

import capston.new_valance.dto.NewsSimpleDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class SearchResponse {

    /**  key = 카테고리 이름, value = 해당 카테고리의 기사 목록  */
    private Map<String, List<NewsSimpleDto>> articlesByCategory;
}
