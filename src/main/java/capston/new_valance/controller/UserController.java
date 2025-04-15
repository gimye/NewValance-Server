package capston.new_valance.controller;

import capston.new_valance.dto.req.OnboardingRequest;
import capston.new_valance.dto.req.UsernameValidationRequest;
import capston.new_valance.dto.res.UsernameCheckResponse;
import capston.new_valance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @PostMapping("/check-username")
    public ResponseEntity<UsernameCheckResponse> checkUsernameDuplicate(
            @Valid @RequestBody UsernameValidationRequest request
    ) {
        boolean isAvailable = userService.isUsernameAvailable(request.getUsername());
        return ResponseEntity.ok(new UsernameCheckResponse(isAvailable));
    }
}
