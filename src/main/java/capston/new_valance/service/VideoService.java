// src/main/java/capston/new_valance/service/VideoService.java
package capston.new_valance.service;

import capston.new_valance.dto.NewsWithVideosDto;
import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.res.VideoListResponse;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.NewsCategory;
import capston.new_valance.model.UserTopTag;
import capston.new_valance.model.UserVideoInteraction;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.UserTopTagRepository;
import capston.new_valance.repository.UserVideoInteractionRepository;
import capston.new_valance.repository.VideoVersionRepository;
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

    /**
     * 메인 엔드포인트: /api/video/{type} 및 /api/video/{type}/{newsId}
     */
    public VideoListResponse getVideosByType(String type, Long newsId, Long userId) {

        // 1) 좋아요(type="likes")일 때
        if ("likes".equalsIgnoreCase(type)) {
            return getLikedVideos(userId, newsId);
        }

        // 2) 추천(type="recommend")일 때
        if ("recommend".equalsIgnoreCase(type)) {
            return getRecommendedVideos(userId, newsId);
        }

        // 3) 카테고리 또는 today
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

        // DTO 변환
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

        // nextNewsId 계산
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

    /**
     * 전체 좋아요한 뉴스를 시간 내림차순으로 조회 → newsId 기준 무한 스크롤(3개씩)
     */
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

    /**
     * 추천 로직
     */
    public VideoListResponse getRecommendedVideos(Long userId, Long newsId) {
        List<UserTopTag> topTags = userTopTagRepository.findTop5ByUserIdOrderByWeightDesc(userId);
        if (topTags.isEmpty()) {
            return VideoListResponse.builder().news(List.of()).nextNewsId(null).build();
        }

        List<Integer> tagIds = topTags.stream()
                .map(UserTopTag::getTagId)
                .toList();

        List<NewsArticle> candidates = newsArticleRepository.findRecommendedArticlesByTagIds(tagIds, 100);

        Set<Long> watched = userVideoInteractionRepository.findTop100ByUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(uv -> uv.getArticle().getArticleId())
                .collect(Collectors.toSet());

        candidates = candidates.stream()
                .filter(a -> !watched.contains(a.getArticleId()))
                .toList();

        if (newsId != null) {
            candidates = candidates.stream()
                    .dropWhile(a -> !Objects.equals(a.getArticleId(), newsId))
                    .skip(1)
                    .toList();
        }

        List<NewsArticle> recommended = candidates.stream().limit(3).toList();

        Long nextId = null;
        if (recommended.size() == 3 && candidates.size() > 3) {
            nextId = candidates.get(3).getArticleId();
        }

        List<NewsWithVideosDto> dtos = recommended.stream()
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
                .news(dtos)
                .nextNewsId(nextId)
                .build();
    }
}
