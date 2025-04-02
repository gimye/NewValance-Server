package capston.new_valance.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AuthController {

    @GetMapping("/api/login/{provider}")
    public void login(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        String redirectUrl = switch (provider.toLowerCase()) {
            case "kakao" -> "/oauth2/authorization/kakao";
            case "naver" -> "/oauth2/authorization/naver";
            case "google" -> "/oauth2/authorization/google";
            default -> throw new IllegalArgumentException("잘못된 소셜로그인 요청: " + provider);
        };
        response.sendRedirect(redirectUrl);
    }
}

