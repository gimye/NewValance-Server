package capston.new_valance.service;

import capston.new_valance.dto.NewsWithVideosDto;
import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.res.VideoListResponse;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.NewsCategory;
import capston.new_valance.model.UserTopTag;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final NewsArticleRepository newsArticleRepository;
    private final VideoVersionRepository videoVersionRepository;
    private final UserTopTagRepository userTopTagRepository;
    private final UserVideoInteractionRepository userVideoInteractionRepository;

    // 1. today / recommend / 카테고리 목록 (무한스크롤)
    @Transactional(readOnly = true)
    public VideoListResponse getVideosByType(String type, Long newsId, Long userId) {

        // 1) 오늘 생성된 영상
        if ("today".equalsIgnoreCase(type)) {
            return getTodayVideos(userId, newsId);
        }

        // 2) 추천
        if ("recommend".equalsIgnoreCase(type)) {
            return getRecommendedVideos(userId, newsId);
        }

        /* likes 타입은 별도 엔드포인트 → 잘못 호출 시 400 */
        if ("liked".equalsIgnoreCase(type)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "likes 타입은 /api/video/liked/{newsId} 엔드포인트를 사용하세요."
            );
        }

        // 3) 카테고리
        Long categoryId = NewsCategory.fromType(type);
        if (categoryId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "요청한 파라미터 형식이 올바르지 않습니다: " + type
            );
        }

        /* 첫 페이지 vs 페이징 */
        List<NewsArticle> articles = (newsId == null)
                ? newsArticleRepository.findTop3ByCategoryIdOrderByPublishedAtDescArticleIdAsc(categoryId)
                : newsArticleRepository.findNext3ByCategoryIdAndNewsId(categoryId, newsId);

        List<NewsWithVideosDto> list = articles.stream()
                .map(article -> toNewsWithVideosDto(article, userId))
                .toList();

        Long nextId = null;
        if (!articles.isEmpty()) {
            Long lastId = articles.get(articles.size() - 1).getArticleId();
            nextId = newsArticleRepository.findNextNewsId(lastId, categoryId).orElse(null);
        }

        return VideoListResponse.builder()
                .news(list)
                .nextNewsId(nextId)
                .build();
    }

    // 2. 좋아요한 ‘단일’ 영상 재생
    @Transactional(readOnly = true)
    public NewsWithVideosDto getLikedVideoDetail(Long userId, Long newsId) {

        if (newsId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "newsId가 필요합니다.");
        }

        boolean liked = userVideoInteractionRepository
                .existsByUserIdAndArticle_ArticleIdAndLikedTrue(userId, newsId);

        if (!liked) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "좋아요 목록에 없는 영상입니다: " + newsId);
        }

        NewsArticle article = newsArticleRepository.findByArticleId(newsId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 기사 없음: " + newsId));

        return toNewsWithVideosDto(article, userId);  // liked=true 상태 포함
    }

    // 3. 오늘(00:00~23:59) 생성된 영상 기사 목록
    private VideoListResponse getTodayVideos(Long userId, Long newsId) {

        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        List<VideoVersion> todayVersions = videoVersionRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        List<Long> articleIds = todayVersions.stream()
                .map(v -> v.getArticle().getArticleId())
                .distinct()
                .toList();

        int startIdx = 0;
        if (newsId != null && articleIds.contains(newsId)) {
            startIdx = articleIds.indexOf(newsId);
        }

        List<Long> pageIds = articleIds.stream()
                .skip(startIdx)
                .limit(3)
                .toList();

        List<NewsWithVideosDto> list = pageIds.stream()
                .map(id -> {
                    NewsArticle article = newsArticleRepository.findByArticleId(id)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "해당 기사 없음: " + id));

                    /* todayVersions 로부터 version 목록 구성 */
                    List<VideoVersionDto> versions = todayVersions.stream()
                            .filter(v -> Objects.equals(v.getArticle().getArticleId(), id))
                            .map(v -> VideoVersionDto.builder()
                                    .versionName(v.getVersionName())
                                    .videoUrl(v.getVideoUrl())
                                    .build())
                            .toList();

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
                .toList();

        Long nextId = null;
        if (startIdx + pageIds.size() < articleIds.size()) {
            nextId = articleIds.get(startIdx + pageIds.size());
        }

        return VideoListResponse.builder()
                .news(list)
                .nextNewsId(nextId)
                .build();
    }

    // 4. 추천 알고리즘 기반 목록
    @Transactional(readOnly = true)
    public VideoListResponse getRecommendedVideos(Long userId, Long newsId) {

        List<UserTopTag> topTags =
                userTopTagRepository.findTop5ByUserIdOrderByWeightDesc(userId);

        if (topTags.isEmpty()) {
            return VideoListResponse.builder()
                    .news(List.of())
                    .nextNewsId(null)
                    .build();
        }

        Set<Integer> tagIds = topTags.stream()
                .map(UserTopTag::getTagId)
                .collect(Collectors.toSet());

        List<NewsArticle> candidates =
                newsArticleRepository.findRecommendedArticlesByTagIds(List.copyOf(tagIds), 100);

        Set<Long> watched = userVideoInteractionRepository
                .findTop100ByUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(uv -> uv.getArticle().getArticleId())
                .collect(Collectors.toSet());

        List<NewsArticle> filtered = candidates.stream()
                .filter(a -> !watched.contains(a.getArticleId()))
                .toList();

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
                .toList();

        Long nextId = null;
        if (start + page.size() < filtered.size()) {
            nextId = filtered.get(start + page.size()).getArticleId();
        }

        List<NewsWithVideosDto> dtos = page.stream()
                .map(a -> toNewsWithVideosDto(a, userId))
                .toList();

        return VideoListResponse.builder()
                .news(dtos)
                .nextNewsId(nextId)
                .build();
    }

    // 5. 공통: NewsArticle → NewsWithVideosDto 변환
    private NewsWithVideosDto toNewsWithVideosDto(NewsArticle article, Long userId) {

        List<VideoVersionDto> versions = videoVersionRepository
                .findByArticle_ArticleIdOrderByVersionNameAsc(article.getArticleId())
                .stream()
                .map(v -> VideoVersionDto.builder()
                        .versionName(v.getVersionName())
                        .videoUrl(v.getVideoUrl())
                        .build())
                .toList();

        boolean liked = (userId != null) && userVideoInteractionRepository
                .existsByUserIdAndArticle_ArticleIdAndLikedTrue(userId, article.getArticleId());

        return NewsWithVideosDto.builder()
                .newsId(article.getArticleId())
                .title(article.getTitle())
                .originalUrl(article.getOriginalUrl())
                .thumbnailUrl(article.getThumbnailUrl())
                .videoVersions(versions)
                .liked(liked)
                .build();
    }
}
