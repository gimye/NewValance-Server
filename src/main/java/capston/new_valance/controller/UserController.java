package capston.new_valance.controller;

import capston.new_valance.dto.NewsSimpleDto;
import capston.new_valance.dto.req.OnboardingRequest;
import capston.new_valance.dto.req.UsernameValidationRequest;
import capston.new_valance.dto.res.UsernameCheckResponse;
import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. 온보딩 PATCH /api/user/onboarding
    @PatchMapping("/onboarding")
    public ResponseEntity<String> updateUsernameAndTags(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid OnboardingRequest request
    ) {
        // 사용자 이름 업데이트
        userService.updateUsername(accessToken, request.getUsername());

        // 온보딩 시 태그 목록이 전달되었다면 태그도 업데이트
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            userService.updateUserTags(accessToken, request.getTags());
        }

        return ResponseEntity.ok("Username and Tags updated successfully.");
    }

    // 2. 닉네임 중복확인 /api/user/check-username
    @PostMapping("/check-username")
    public ResponseEntity<UsernameCheckResponse> checkUsernameDuplicate(
            @Valid @RequestBody UsernameValidationRequest request
    ) {
        boolean isAvailable = userService.isUsernameAvailable(request.getUsername());
        return ResponseEntity.ok(new UsernameCheckResponse(isAvailable));
    }

    // 3. 좋아요 뉴스 영상 반환 GET /api/user/liked
    @GetMapping("/liked")
    public ResponseEntity<Map<String, Object>> getLikedNews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Long userId = userPrincipal.getUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "watchedAt"));
        Page<NewsSimpleDto> likedPage = userService.getLikedNews(userId, pageable);

        Map<String, Object> responseBody = Map.of(
                "_embedded", Map.of("newsSimpleDtoList", likedPage.getContent()),
                "page", Map.of(
                        "size", likedPage.getSize(),
                        "totalElements", likedPage.getTotalElements(),
                        "totalPages", likedPage.getTotalPages(),
                        "number", likedPage.getNumber()
                )
        );

        return ResponseEntity.ok(responseBody);
    }
}
