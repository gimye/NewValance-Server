package capston.new_valance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/login")
public class AuthController {

    @GetMapping("/{provider}")
    public void login(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        String redirectUrl = getRedirectUrl(provider);
        response.sendRedirect(redirectUrl);
    }

    private String getRedirectUrl(String provider) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> "/oauth2/authorization/kakao";
            case "naver" -> "/oauth2/authorization/naver";
            case "google" -> "/oauth2/authorization/google";
            default -> throw new IllegalArgumentException("지원되지 않는 소셜 로그인 제공자: " + provider);
        };
    }
}

