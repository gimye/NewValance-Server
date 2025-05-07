package capston.new_valance.service;

import capston.new_valance.dto.NewsSimpleDto;
import capston.new_valance.dto.NewsStandResponseDto;
import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.res.BannerResponse;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.VideoVersionRepository;
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
    private final VideoVersionRepository videoVersionRepository;

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
                3L, "세계",
                4L, "생활/문화",
                5L, "IT/과학",
                6L, "사회"
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

    /**
     * 임시 Banner API - 전체 뉴스 중 최신 3개를 조회하여
     * title, 썸네일, videoVersions(버전명, videoUrl 등 포함)를 반환합니다.
     */
    public List<BannerResponse> getBanner() {
        // 전체 뉴스 중 상위 3개 (출판일 내림차순, 동일 시 articleId 오름차순) 조회
        List<NewsArticle> articles = newsArticleRepository.findTop3ByOrderByPublishedAtDescArticleIdAsc();

        return articles.stream().map(article -> {
            // 각 뉴스에 해당하는 영상 버전 정보 조회 (정렬 조건은 필요에 따라 결정)
            List<VideoVersionDto> videoVersions = videoVersionRepository
                    .findByArticle_ArticleIdOrderByVersionNameAsc(article.getArticleId())
                    .stream()
                    .map(v -> VideoVersionDto.builder()
                            .versionName(v.getVersionName())
                            .videoUrl(v.getVideoUrl())
                            .build())
                    .collect(Collectors.toList());

            return BannerResponse.builder()
                    .articleId(article.getArticleId())
                    .title(article.getTitle())
                    .thumbnailUrl(article.getThumbnailUrl())
                    .videoVersions(videoVersions)
                    .build();
        }).collect(Collectors.toList());
    }


    private NewsSimpleDto toSimpleDto(NewsArticle article) {
        return NewsSimpleDto.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .thumbnailUrl(article.getThumbnailUrl())
                .build();
    }
}
