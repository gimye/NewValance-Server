package capston.new_valance.controller;

import capston.new_valance.dto.NewsWithVideosDto;
import capston.new_valance.dto.res.VideoListResponse;
import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;


    //  1. today / recommend / 카테고리 목록 (무한스크롤)
    @GetMapping(value = "/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoListResponse> listVideos(
            @PathVariable("type") String type,
            Authentication authentication
    ) {
        return listVideosPaged(type, null, authentication);
    }

    // 2. /api/video/{type}/{newsId} (다음 페이지)
    @GetMapping(value = "/{type}/{newsId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VideoListResponse> listVideosPaged(
            @PathVariable("type") String type,
            @PathVariable(value = "newsId", required = false) Long newsId,
            Authentication authentication
    ) {
        // liked 타입은 전용 엔드포인트 사용
        if ("liked".equalsIgnoreCase(type)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "liked 타입은 /api/video/liked/{newsId} 엔드포인트를 사용하세요."
            );
        }

        Long userId = extractUserId(authentication);

        if ("recommend".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(videoService.getRecommendedVideos(userId, newsId));
        }

        return ResponseEntity.ok(videoService.getVideosByType(type, newsId, userId));
    }

    // 3. 좋아요한 ‘단일’ 영상 재생
    @GetMapping(value = "/liked/{newsId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NewsWithVideosDto> playLikedVideo(
            @PathVariable("newsId") Long newsId,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(videoService.getLikedVideoDetail(userId, newsId));
    }

    // 공통: 인증 객체에서 userId 추출
    private Long extractUserId(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return null;
    }
}
