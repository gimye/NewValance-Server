package capston.new_valance.controller;

import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.model.User;
import capston.new_valance.repository.UserRepository;
import lombok.extern.slf4j.Slf4j; // Lombok을 사용한 로깅
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class TokenController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public TokenController(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh")
    public Map<String, String> refreshToken(@RequestBody Map<String, String> tokenRequest) {
        String refreshToken = tokenRequest.get("refresh_token");

        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Refresh token is missing in the request");
            throw new IllegalArgumentException("Refresh token is missing");
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Invalid refresh token: {}", refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        log.debug("Extracted userId from refresh token: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        log.info("Generated new tokens for user: {}", user.getEmail());

        return Map.of(
                "access_token", newAccessToken,
                "refresh_token", newRefreshToken,
                "message", "Tokens refreshed successfully"
        );
    }
}

