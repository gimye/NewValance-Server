package capston.new_valance.controller;

import capston.new_valance.dto.req.ProfilePatchRequest;
import capston.new_valance.dto.res.DailyWatchCountResponse;
import capston.new_valance.dto.res.ProfileResponse;
import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    /* ================================
       0) 조회: GET /api/profile
       ================================ */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        ProfileResponse profile = profileService.getProfile(principal.getUserId());
        return ResponseEntity.ok(profile);
    }

    /* ================================
       1) 수정: PATCH /api/profile
       ================================ */
    @PatchMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "username", required = false) String username,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        // 1) 파일 타입 검증
        if (profileImage != null &&
                !profileImage.isEmpty() &&
                !profileImage.getContentType().startsWith("image/")
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미지 파일만 업로드 가능합니다."
            );
        }

        // 2) 빌더로 DTO 조립
        ProfilePatchRequest req = ProfilePatchRequest.builder()
                .username(username)
                .profileImage(profileImage)
                .build();

        // 3) 서비스 호출
        ProfileResponse updated = profileService.updateProfile(principal.getUserId(), req);
        return ResponseEntity.ok(updated);
    }

    /* ================================
       2) 주간 조회: GET /api/profile/week
       ================================ */
    @GetMapping("/week")
    public ResponseEntity<List<List<DailyWatchCountResponse>>> getWeeklyWatchCounts(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<List<DailyWatchCountResponse>> result = profileService.getWeeklyWatchCounts(principal.getUserId());
        return ResponseEntity.ok(result);
    }
}
