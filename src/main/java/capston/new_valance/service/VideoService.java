package capston.new_valance.service;

import capston.new_valance.dto.NewsWithVideosDto;
import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.res.VideoListResponse;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.NewsCategory;
import capston.new_valance.model.UserTopTag;
import capston.new_valance.model.UserVideoInteraction;
import capston.new_valance.model.VideoVersion;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.UserTopTagRepository;
import capston.new_valance.repository.UserVideoInteractionRepository;
import capston.new_valance.repository.VideoVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final NewsArticleRepository newsArticleRepository;
    private final VideoVersionRepository videoVersionRepository;
    private final UserTopTagRepository userTopTagRepository;
    private final UserVideoInteractionRepository userVideoInteractionRepository;

    /**
     * 메인 엔드포인트: /api/video/{type} 및 /api/video/{type}/{newsId}
     */
    @Transactional(readOnly = true)
    public VideoListResponse getVideosByType(String type, Long newsId, Long userId) {
        // 1) 오늘 생성된 영상만 (type="today")
        if ("today".equalsIgnoreCase(type)) {
            return getTodayVideos(userId, newsId);
        }
        // 2) 좋아요(type="likes")
        if ("likes".equalsIgnoreCase(type)) {
            return getLikedVideos(userId, newsId);
        }
        // 3) 추천(type="recommend")
        if ("recommend".equalsIgnoreCase(type)) {
            return getRecommendedVideos(userId, newsId);
        }
        // 4) 카테고리
        Long categoryId = NewsCategory.fromType(type);
        if (categoryId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "요청한 파라미터 형식이 올바르지 않습니다: " + type
            );
        }
        // 첫 페이지 vs. 페이징
        List<NewsArticle> articles = (newsId == null)
                ? newsArticleRepository.findTop3ByCategoryIdOrderByPublishedAtDescArticleIdAsc(categoryId)
                : newsArticleRepository.findNext3ByCategoryIdAndNewsId(categoryId, newsId);

        List<NewsWithVideosDto> list = articles.stream()
                .map(article -> {
                    List<VideoVersionDto> videoVersions = videoVersionRepository
                            .findByArticle_ArticleIdOrderByVersionNameAsc(article.getArticleId())
                            .stream()
                            .map(v -> VideoVersionDto.builder()
                                    .versionName(v.getVersionName())
                                    .videoUrl(v.getVideoUrl())
                                    .build())
                            .collect(Collectors.toList());

                    boolean liked = userId != null && userVideoInteractionRepository
                            .existsByUserIdAndArticle_ArticleIdAndLikedTrue(userId, article.getArticleId());

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

        Long nextId = null;
        if (!articles.isEmpty()) {
            Long last = articles.get(articles.size() - 1).getArticleId();
            nextId = newsArticleRepository.findNextNewsId(last, categoryId).orElse(null);
        }

        return VideoListResponse.builder()
                .news(list)
                .nextNewsId(nextId)
                .build();
    }

    /** 오늘(00:00~23:59) 생성된 영상 기사만 반환 (최신순 페이징) */
    private VideoListResponse getTodayVideos(Long userId, Long newsId) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        // 오늘 생성된 모든 영상 버전
        List<VideoVersion> todayVersions = videoVersionRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        // 중복 제거한 articleId 리스트
        List<Long> articleIds = todayVersions.stream()
                .map(v -> v.getArticle().getArticleId())
                .distinct()
                .collect(Collectors.toList());

        int startIdx = 0;
        if (newsId != null && articleIds.contains(newsId)) {
            startIdx = articleIds.indexOf(newsId);
        }

        List<Long> pageIds = articleIds.stream()
                .skip(startIdx)
                .limit(3)
                .collect(Collectors.toList());

        List<NewsWithVideosDto> list = pageIds.stream()
                .map(id -> {
                    NewsArticle article = newsArticleRepository.findByArticleId(id)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "해당 기사 없음: " + id));

                    List<VideoVersionDto> versions = todayVersions.stream()
                            .filter(v -> Objects.equals(v.getArticle().getArticleId(), id))
                            .map(v -> VideoVersionDto.builder()
                                    .versionName(v.getVersionName())
                                    .videoUrl(v.getVideoUrl())
                                    .build())
                            .collect(Collectors.toList());

                    boolean liked = userId != null && userVideoInteractionRepository
                            .existsByUserIdAndArticle_ArticleIdAndLikedTrue(userId, id);

                    return NewsWithVideosDto.builder()
                            .newsId(id)
                            .title(article.getTitle())
                            .originalUrl(article.getOriginalUrl())
                            .thumbnailUrl(article.getThumbnailUrl())
                            .videoVersions(versions)
                            .liked(liked)
                            .build();
                })
                .collect(Collectors.toList());

        Long nextId = null;
        if (startIdx + pageIds.size() < articleIds.size()) {
            nextId = articleIds.get(startIdx + pageIds.size());
        }

        return VideoListResponse.builder()
                .news(list)
                .nextNewsId(nextId)
                .build();
    }

    /** 전체 좋아요한 영상 무한 스크롤(3개씩) */
    @Transactional(readOnly = true)
    private VideoListResponse getLikedVideos(Long userId, Long newsId) {
        List<UserVideoInteraction> likedAll = userVideoInteractionRepository
                .findByUserIdAndLikedTrueOrderByWatchedAtDesc(userId);

        int start = 0;
        if (newsId != null) {
            for (int i = 0; i < likedAll.size(); i++) {
                if (Objects.equals(likedAll.get(i).getArticle().getArticleId(), newsId)) {
                    start = i + 1;
                    break;
                }
            }
        }

        List<UserVideoInteraction> page = likedAll.stream()
                .skip(start)
                .limit(3)
                .collect(Collectors.toList());

        List<NewsWithVideosDto> dtos = page.stream().map(inter -> {
            var a = inter.getArticle();
            List<VideoVersionDto> versions = videoVersionRepository
                    .findByArticle_ArticleIdOrderByVersionNameAsc(a.getArticleId())
                    .stream()
                    .map(v -> VideoVersionDto.builder()
                            .versionName(v.getVersionName())
                            .videoUrl(v.getVideoUrl())
                            .build())
                    .collect(Collectors.toList());

            return NewsWithVideosDto.builder()
                    .newsId(a.getArticleId())
                    .title(a.getTitle())
                    .originalUrl(a.getOriginalUrl())
                    .thumbnailUrl(a.getThumbnailUrl())
                    .videoVersions(versions)
                    .liked(true)
                    .build();
        }).collect(Collectors.toList());

        Long nextId = null;
        if (start + page.size() < likedAll.size()) {
            nextId = likedAll.get(start + page.size()).getArticle().getArticleId();
        }

        return VideoListResponse.builder()
                .news(dtos)
                .nextNewsId(nextId)
                .build();
    }

    /** 추천 알고리즘 기반 영상 3개 페이징 */
    @Transactional(readOnly = true)
    public VideoListResponse getRecommendedVideos(Long userId, Long newsId) {
        List<UserTopTag> topTags = userTopTagRepository.findTop5ByUserIdOrderByWeightDesc(userId);
        if (topTags.isEmpty()) {
            return VideoListResponse.builder().news(List.of()).nextNewsId(null).build();
        }

        Set<Integer> tagIds = topTags.stream()
                .map(UserTopTag::getTagId)
                .collect(Collectors.toSet());

        List<NewsArticle> candidates = newsArticleRepository.findRecommendedArticlesByTagIds(List.copyOf(tagIds), 100);

        Set<Long> watched = userVideoInteractionRepository.findTop100ByUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(uv -> uv.getArticle().getArticleId())
                .collect(Collectors.toSet());

        List<NewsArticle> filtered = candidates.stream()
                .filter(a -> !watched.contains(a.getArticleId()))
                .collect(Collectors.toList());

        int start = 0;
        if (newsId != null) {
            for (int i = 0; i < filtered.size(); i++) {
                if (Objects.equals(filtered.get(i).getArticleId(), newsId)) {
                    start = i + 1;
                    break;
                }
            }
        }

        List<NewsArticle> page = filtered.stream()
                .skip(start)
                .limit(3)
                .collect(Collectors.toList());

        Long nextId = null;
        if (start + page.size() < filtered.size()) {
            nextId = filtered.get(start + page.size()).getArticleId();
        }

        List<NewsWithVideosDto> dtos = page.stream()
                .map(article -> {
                    List<VideoVersionDto> versions = videoVersionRepository
                            .findByArticle_ArticleIdOrderByVersionNameAsc(article.getArticleId())
                            .stream()
                            .map(v -> VideoVersionDto.builder()
                                    .versionName(v.getVersionName())
                                    .videoUrl(v.getVideoUrl())
                                    .build())
                            .collect(Collectors.toList());

                    boolean liked = userId != null && userVideoInteractionRepository
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
                .collect(Collectors.toList());

        return VideoListResponse.builder()
                .news(dtos)
                .nextNewsId(nextId)
                .build();
    }
}
