package capston.new_valance.controller;

import capston.new_valance.dto.NewsSimpleDto;
import capston.new_valance.dto.NewsStandResponseDto;
import capston.new_valance.dto.res.BannerResponse;
import capston.new_valance.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsArticleController {

    private final NewsArticleService newsService;

    // 1. 홈 가판대 - 카테고리별 최신 뉴스 10개씩 반환 GET /api/news/home
    @GetMapping("/home")
    public ResponseEntity<List<NewsStandResponseDto>> getNewsStand() {
        return ResponseEntity.ok(newsService.getNewsStand());
    }

    // 2. 홈 배너 API GET /api/news/banner
    @GetMapping("/banner")
    public ResponseEntity<List<BannerResponse>> getBanner() {
        return ResponseEntity.ok(newsService.getBanner());
    }

    // 3. 카테고리별 뉴스 조회 API GET /api/news/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PagedModel<NewsSimpleDto>> getNewsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<NewsSimpleDto> newsPage = newsService.getNewsByCategory(categoryId, pageable);

        PagedModel<NewsSimpleDto> pagedModel = PagedModel.of(
                newsPage.getContent(),
                new PagedModel.PageMetadata(
                        newsPage.getSize(),
                        newsPage.getNumber(),
                        newsPage.getTotalElements(),
                        newsPage.getTotalPages()
                )
        );

        return ResponseEntity.ok(pagedModel);
    }
}
