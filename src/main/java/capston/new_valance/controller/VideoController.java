package capston.new_valance.controller;

import capston.new_valance.dto.res.VideoListResponse;
import capston.new_valance.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    // GET /api/video/{type} - 해당 타입의 최신 뉴스 3개 반환
    @GetMapping("/{type}")
    public ResponseEntity<VideoListResponse> getVideosByType(@PathVariable("type") String type) {
        VideoListResponse response = videoService.getVideosByType(type, null);
        return ResponseEntity.ok(response);
    }

    // GET /api/video/{type}/{newsId} - 기준 newsId 이후의 뉴스 3개 반환
    @GetMapping("/{type}/{newsId}")
    public ResponseEntity<VideoListResponse> getVideosByTypeAndNewsId(
            @PathVariable("type") String type,
            @PathVariable("newsId") Long newsId) {
        VideoListResponse response = videoService.getVideosByType(type, newsId);
        return ResponseEntity.ok(response);
    }
}
