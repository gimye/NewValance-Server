package capston.new_valance.controller;

import capston.new_valance.dto.NewsWithVideosDto;
import capston.new_valance.dto.res.SearchResponse;
import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;


    // 1. 제목 검색 GET /api/search?query=
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SearchResponse> searchByTitle(
            @RequestParam("query") String query
    ) {
        return ResponseEntity.ok(searchService.searchByTitle(query));
    }


    // 2. 재생: 전체 영상 버전별 S3 URL + 좋아요 여부 반환 GET /api/search/play/{articleId}
    @GetMapping(
            path = "/{articleId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<NewsWithVideosDto> playAllVersions(
            @PathVariable("articleId") Long articleId,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.ok(searchService.getNewsWithVideos(articleId, userId));
    }
}
