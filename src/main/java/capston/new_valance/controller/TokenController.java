package capston.new_valance.controller;

import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.model.User;
import capston.new_valance.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> tokenRequest) {
        String refreshToken = tokenRequest.get("refresh_token");

        // refresh token이 없으면 400 Bad Request 응답
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is missing"));
        }

        // refresh token 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
        }

        // refresh token에서 사용자 식별 정보(예, userId) 추출
        Long userId = jwtUtil.getUserId(refreshToken);

        // DB에서 사용자 조회
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }
        User user = optionalUser.get();

        // 새로운 access token 발급
        String newAccessToken = jwtUtil.generateToken(user);

        // (옵션) 새로운 refresh token도 발급할 수 있음.
        // String newRefreshToken = jwtUtil.generateRefreshToken(user);
        // 필요시 refresh token 저장소 갱신 로직 추가

        return ResponseEntity.ok(Map.of(
                "access_token", newAccessToken,
                "message", "Access token refreshed successfully"
        ));
    }
}
