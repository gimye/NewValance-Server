package capston.new_valance.controller;

import capston.new_valance.dto.req.OnboardingRequest;
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
    public ResponseEntity<String> updateUsername(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody @Valid OnboardingRequest request
    ) {
        userService.updateUsername(accessToken, request.getUsername());
        return ResponseEntity.ok("Username updated successfully.");
    }

    @PostMapping("/check-username")
    public ResponseEntity<UsernameCheckResponse> checkUsernameDuplicate(
            @Valid @RequestBody OnboardingRequest request
    ) {
        boolean isAvailable = userService.isUsernameAvailable(request.getUsername());
        return ResponseEntity.ok(new UsernameCheckResponse(isAvailable));
    }


}
