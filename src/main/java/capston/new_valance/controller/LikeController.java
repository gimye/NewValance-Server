package capston.new_valance.controller;

import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/video")
public class LikeController {

    private final LikeService likeService;

    // 1. 좋아요, 좋아요 취소 POST /api/video/{newsId}/like
    @PostMapping("/{newsId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable("newsId") Long newsId,
                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean liked = likeService.toggleLike(newsId, userPrincipal.getUserId());

        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "message", liked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
        ));
    }
}
