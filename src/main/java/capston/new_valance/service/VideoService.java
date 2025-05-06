package capston.new_valance.service;

import capston.new_valance.dto.NewsWithVideosDto;
import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.res.VideoListResponse;
import capston.new_valance.model.*;
import capston.new_valance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final NewsArticleRepository newsArticleRepository;
    private final VideoVersionRepository videoVersionRepository;
    private final UserTopTagRepository userTopTagRepository;
    private final UserVideoInteractionRepository userVideoInteractionRepository;

    /* ------------------------------------------------------------------
       1)  카테고리·추천 구분하여 영상 리스트를 가져오는 메인 엔드포인트
           ✔ VideoController 가 호출하는 바로 그 시그니처
    ------------------------------------------------------------------ */
    public VideoListResponse getVideosByType(String type, Long newsId, Long userId) {

        /* 추천 목록 */
        if ("recommend".equalsIgnoreCase(type)) {
            return getRecommendedVideos(userId, newsId);
        }

        /* 일반 카테고리 */
        Long categoryId = NewsCategory.fromType(type);
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid type");
        }

        /* 첫 페이지 vs. 페이징 */
        List<NewsArticle> articles = (newsId == null)
                ? newsArticleRepository.findTop3ByCategoryIdOrderByPublishedAtDescArticleIdAsc(categoryId)
                : newsArticleRepository.findNext3ByCategoryIdAndNewsId(categoryId, newsId);

        List<NewsWithVideosDto> newsList = articles.stream()
                .map(article -> {

                    /* 영상 버전 리스트 */
                    List<VideoVersionDto> videoVersions = videoVersionRepository
                            .findByArticle_ArticleIdOrderByVersionNameAsc(article.getArticleId())
                            .stream()
                            .map(v -> VideoVersionDto.builder()
                                    .versionName(v.getVersionName())
                                    .videoUrl(v.getVideoUrl())
                                    .build())
                            .collect(Collectors.toList());

                    /* 좋아요 여부 (시청 기록 = ‘기사’ 기준으로 변경됨) */
                    boolean liked = false;
                    if (userId != null) {
                        liked = userVideoInteractionRepository
                                .existsByUserIdAndArticle_ArticleIdAndLikedTrue(userId, article.getArticleId());
                    }

                    return NewsWithVideosDto.builder()
                            .newsId(article.getArticleId())
                            .title(article.getTitle())
                            .originalUrl(article.getOriginalUrl())
                            .thumbnailUrl(article.getThumbnailUrl())
                            .videoVersions(videoVersions)
                            .liked(liked)
                            .build();
                })
                .collect(Collectors.toList());

        /* 다음 페이지용 newsId 계산 */
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

    /* ------------------------------------------------------------------
       2)  ‘추천’ 로직  (앞서 수정한 기사-기준 시청 제외 버전)
    ------------------------------------------------------------------ */
    public VideoListResponse getRecommendedVideos(Long userId, Long newsId) {

        List<UserTopTag> topTags = userTopTagRepository.findTop5ByUserIdOrderByWeightDesc(userId);
        if (topTags.isEmpty()) {
            return VideoListResponse.builder().news(List.of()).nextNewsId(null).build();
        }

        List<Integer> tagIds = topTags.stream()
                .map(UserTopTag::getTagId)
                .toList();

        /* 태그 기반 후보 100개 */
        List<NewsArticle> candidates =
                newsArticleRepository.findRecommendedArticlesByTagIds(tagIds, 100);

        /* 최근 본 ‘기사’ 100개는 제외 */
        Set<Long> watchedArticleIds =
                userVideoInteractionRepository.findTop100ByUserIdOrderByWatchedAtDesc(userId)
                        .stream()
                        .map(uv -> uv.getArticle().getArticleId())
                        .collect(Collectors.toSet());

        candidates = candidates.stream()
                .filter(a -> !watchedArticleIds.contains(a.getArticleId()))
                .toList();

        /* 무한 스크롤: newsId 이후부터 가져오기 */
        if (newsId != null) {
            candidates = candidates.stream()
                    .dropWhile(a -> !Objects.equals(a.getArticleId(), newsId))
                    .skip(1)
                    .toList();
        }

        List<NewsArticle> recommended = candidates.stream()
                .limit(3)
                .toList();

        Long nextNewsId = null;
        if (recommended.size() == 3 && candidates.size() > 3) {
            nextNewsId = candidates.get(3).getArticleId();
        }

        /* DTO 변환 */
        List<NewsWithVideosDto> newsDtos = recommended.stream()
                .map(article -> {
                    List<VideoVersionDto> versions = videoVersionRepository
                            .findByArticle_ArticleIdOrderByVersionNameAsc(article.getArticleId())
                            .stream()
                            .map(v -> VideoVersionDto.builder()
                                    .versionName(v.getVersionName())
                                    .videoUrl(v.getVideoUrl())
                                    .build())
                            .toList();

                    boolean liked = userVideoInteractionRepository
                            .existsByUserIdAndArticle_ArticleIdAndLikedTrue(userId, article.getArticleId());

                    return NewsWithVideosDto.builder()
                            .newsId(article.getArticleId())
                            .title(article.getTitle())
                            .originalUrl(article.getOriginalUrl())
                            .thumbnailUrl(article.getThumbnailUrl())
                            .videoVersions(versions)
                            .liked(liked)
                            .build();
                })
                .toList();

        return VideoListResponse.builder()
                .news(newsDtos)
                .nextNewsId(nextNewsId)
                .build();
    }
}
