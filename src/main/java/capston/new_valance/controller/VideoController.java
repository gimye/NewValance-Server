package capston.new_valance.controller;

import capston.new_valance.dto.res.VideoListResponse;
import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    // 1. 타입별 영상 재생 /api/video/{type}
    @GetMapping("/{type}")
    public ResponseEntity<VideoListResponse> getVideosByType(
            @PathVariable("type") String type,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        if ("recommend".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(videoService.getRecommendedVideos(userId, null));
        } else {
            return ResponseEntity.ok(videoService.getVideosByType(type, null, userId));
        }
    }

    // 타입, 뉴스 id 별 영상 재생 /api/video/{type}/{newsId}
    @GetMapping("/{type}/{newsId}")
    public ResponseEntity<VideoListResponse> getVideosByTypeAndNewsId(
            @PathVariable("type") String type,
            @PathVariable("newsId") Long newsId,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        if ("recommend".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(videoService.getRecommendedVideos(userId, newsId));
        } else {
            return ResponseEntity.ok(videoService.getVideosByType(type, newsId, userId));
        }
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return null;
    }
}