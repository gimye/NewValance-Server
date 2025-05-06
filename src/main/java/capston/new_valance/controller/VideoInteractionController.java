package capston.new_valance.controller;

import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.VideoInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/video")
public class VideoInteractionController {

    private final VideoInteractionService interactionService;

    @PostMapping("/{articleId}/complete")
    public ResponseEntity<?> completeArticleVideo(
            @PathVariable("articleId") Long articleId,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "유효하지 않은 인증입니다.",
                    "updatedTodayViewCount", false
            ));
        }

        Long userId = principal.getUserId();
        int todayViewCount = interactionService.handleArticleComplete(userId, articleId);

        return ResponseEntity.ok(Map.of(
                "message", "영상 시청 완료가 처리되었습니다.",
                "updatedTodayViewCount", true,
                "todayViewCount", todayViewCount
        ));
    }
}
