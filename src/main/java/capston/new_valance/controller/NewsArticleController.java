package capston.new_valance.controller;

import capston.new_valance.dto.req.CreateVideoRequest;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    @PostMapping("/metadata")
    public ResponseEntity<NewsArticle> createNewsArticle(@RequestBody CreateVideoRequest request) {
        NewsArticle createdArticle = newsArticleService.createNewsArticle(request);
        return ResponseEntity.ok(createdArticle);
    }
}
