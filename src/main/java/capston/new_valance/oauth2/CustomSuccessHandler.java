package capston.new_valance.oauth2;

import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        if (!(oAuth2User instanceof CustomOAuth2User customUser)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 인증 실패");
            return;
        }

        User user = customUser.getUser();

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // JSON 응답 구성
        Map<String, Object> json = new HashMap<>();
        json.put("access_token", accessToken);
        json.put("refresh_token", refreshToken);
        json.put("isNew", customUser.isNewUser());
        json.put("expireIn", 3600); // 1시간 (초 단위)

        Map<String, Object> userJson = new HashMap<>();
        userJson.put("userId", user.getUserId());
        userJson.put("email", user.getEmail());
        userJson.put("username", user.getUsername());
        userJson.put("profileImage", user.getProfilePictureUrl());

        json.put("user", userJson);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(json));
    }
}
