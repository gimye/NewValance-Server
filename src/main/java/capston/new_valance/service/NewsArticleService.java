package capston.new_valance.service;

import capston.new_valance.dto.NewsSimpleDto;
import capston.new_valance.dto.NewsStandResponseDto;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;

    /**
     * 카테고리별 뉴스 조회 (페이지네이션 적용)
     */
    public Page<NewsSimpleDto> getNewsByCategory(Long categoryId, Pageable pageable) {
        // 페이지네이션과 정렬은 service 또는 Repository에서 처리합니다.
        Page<NewsArticle> page = newsArticleRepository.findByCategoryId(categoryId, pageable);
        return page.map(this::toSimpleDto);
    }

    public List<NewsStandResponseDto> getNewsStand() {
        Map<Long, String> categoryMap = new TreeMap<>(Map.of(
                1L, "정치",
                2L, "경제",
                3L, "국제",
                4L, "문화",
                5L, "사회",
                6L, "IT/과학"
        ));

        return categoryMap.entrySet().stream()
                .map(entry -> {
                    Long categoryId = entry.getKey();
                    String categoryName = entry.getValue();
                    List<NewsSimpleDto> newsList = newsArticleRepository
                            .findTop10ByCategoryIdOrderByPublishedAtDesc(categoryId)
                            .stream()
                            .map(this::toSimpleDto)
                            .collect(Collectors.toList());

                    return NewsStandResponseDto.builder()
                            .categoryId(categoryId)
                            .categoryName(categoryName)
                            .newsList(newsList)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private NewsSimpleDto toSimpleDto(NewsArticle article) {
        return NewsSimpleDto.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .thumbnailUrl(article.getThumbnailUrl())
                .build();
    }
}
