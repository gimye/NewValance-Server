package capston.new_valance.controller;

import capston.new_valance.dto.NewsSimpleDto;
import capston.new_valance.dto.NewsStandResponseDto;
import capston.new_valance.dto.res.BannerResponseDto;
import capston.new_valance.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsArticleController {

    private final NewsArticleService newsService;

    // 홈 가판대 API - 카테고리별 최신 뉴스 10개씩 반환
    @GetMapping("/home")
    public ResponseEntity<List<NewsStandResponseDto>> getNewsStand() {
        return ResponseEntity.ok(newsService.getNewsStand());
    }

    // 홈 배너 API - (임시) 랜덤 3개의
    @GetMapping("/banner")
    public ResponseEntity<List<BannerResponseDto>> getBanner() {
        return ResponseEntity.ok(newsService.getBanner());
    }

    // 카테고리별 뉴스 조회 API (페이징 적용)
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
