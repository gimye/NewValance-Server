// src/main/java/capston/new_valance/service/SearchService.java
package capston.new_valance.service;

import capston.new_valance.dto.NewsSimpleDto;
import capston.new_valance.dto.NewsWithVideosDto;
import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.res.SearchResponse;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.VideoVersion;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.UserVideoInteractionRepository;
import capston.new_valance.repository.VideoVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final NewsArticleRepository newsArticleRepository;
    private final VideoVersionRepository videoVersionRepository;
    private final UserVideoInteractionRepository userVideoInteractionRepository;

    //제목에 query가 포함된 뉴스들을 최신순으로 NewsSimpleDto 리스트 반환
    @Transactional(readOnly = true)
    public SearchResponse searchByTitle(String query) {
        List<NewsArticle> articles = newsArticleRepository
                .findByTitleContainingIgnoreCaseOrderByPublishedAtDesc(query);

        List<NewsSimpleDto> dtos = articles.stream()
                .map(a -> NewsSimpleDto.builder()
                        .articleId(a.getArticleId())
                        .title(a.getTitle())
                        .thumbnailUrl(a.getThumbnailUrl())
                        .build())
                .collect(Collectors.toList());

        return SearchResponse.builder()
                .articles(dtos)
                .build();
    }

    // 단일 articleId에 대한 전체 VideoVersionDto 목록 + liked 여부 반환
    @Transactional(readOnly = true)
    public NewsWithVideosDto getNewsWithVideos(Long articleId, Long userId) {
        // 1) article 조회
        NewsArticle article = newsArticleRepository.findByArticleId(articleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "해당 기사를 찾을 수 없습니다: " + articleId
                ));

        // 2) video versions 모두 조회
        List<VideoVersionDto> versions = videoVersionRepository
                .findByArticle_ArticleIdOrderByVersionNameAsc(articleId)
                .stream()
                .map(v -> VideoVersionDto.builder()
                        .versionName(v.getVersionName())
                        .videoUrl(v.getVideoUrl())
                        .build())
                .collect(Collectors.toList());

        // 3) 사용자가 좋아요를 눌렀는지 확인 (interaction.liked)
        boolean liked = (userId != null)
                && userVideoInteractionRepository
                .existsByUserIdAndArticle_ArticleIdAndLikedTrue(userId, articleId);

        // 4) DTO 조립
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
