package capston.new_valance.service;

import capston.new_valance.dto.NewsWithVideosDto;
import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.res.VideoListResponse;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.VideoVersionRepository;
import capston.new_valance.model.NewsCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final NewsArticleRepository newsArticleRepository;
    private final VideoVersionRepository videoVersionRepository;

    public VideoListResponse getVideosByType(String type, Long newsId) {
        // URL의 영문 타입을 NewsCategory에서 내부 categoryId로 변환 (예: "economy" → 2)
        Long categoryId = NewsCategory.fromType(type);
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid type");
        }

        List<NewsArticle> articles;
        if (newsId == null) {
            // newsId 없이 요청 시 : 최신 뉴스 3개를 조회합니다.
            articles = newsArticleRepository.findTop3ByCategoryIdOrderByPublishedAtDescArticleIdAsc(categoryId);
        } else {
            // newsId가 있을 때 : 기준 뉴스 이후 조건에 부합하는 3개 뉴스를 조회합니다.
            articles = newsArticleRepository.findNext3ByCategoryIdAndNewsId(categoryId, newsId);
        }

        List<NewsWithVideosDto> newsList = articles.stream().map(article -> {
            List<VideoVersionDto> videoVersions = videoVersionRepository
                    .findByArticle_ArticleIdOrderByVersionNameAsc(article.getArticleId())
                    .stream()
                    .map(v -> VideoVersionDto.builder()
                            .versionName(v.getVersionName())
                            .videoUrl(v.getVideoUrl())
                            .build())
                    .collect(Collectors.toList());

            return NewsWithVideosDto.builder()
                    .newsId(article.getArticleId())
                    .title(article.getTitle())
                    .originalUrl(article.getOriginalUrl())
                    .thumbnailUrl(article.getThumbnailUrl())  // 썸네일 URL 포함
                    .videoVersions(videoVersions)
                    .build();
        }).collect(Collectors.toList());

        Long nextNewsId = null;
        if (!articles.isEmpty()) {
            Long lastId = articles.get(articles.size() - 1).getArticleId();
            nextNewsId = newsArticleRepository.findNextNewsId(lastId, categoryId).orElse(null);
        }

        return VideoListResponse.builder()
                .news(newsList)
                .nextNewsId(nextNewsId)
                .build();
    }
}
