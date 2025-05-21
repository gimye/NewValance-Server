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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

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
                                        Authentication authentication)
            throws IOException, ServletException {

        // Principal이 CustomOAuth2User가 아니면 에러 처리
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        if (!(oAuth2User instanceof CustomOAuth2User customUser)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 인증 실패");
            return;
        }

        User user = customUser.getUser();

        // 1) JWT 토큰 생성
        String accessToken  = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // 2) WebView에 보낼 페이로드 데이터 구성
        Map<String, Object> userJson = new HashMap<>();
        userJson.put("userId",       user.getUserId());
        userJson.put("email",        user.getEmail());
        userJson.put("username",     user.getUsername());
        userJson.put("profileImage", user.getProfilePictureUrl());

        Map<String, Object> payload = new HashMap<>();
        payload.put("access_token",  accessToken);
        payload.put("refresh_token", refreshToken);
        payload.put("grantType",     "Bearer");
        payload.put("expiresIn",     3600);
        payload.put("isNew",         customUser.isNewUser());
        payload.put("user",          userJson);

        // 3) JSON 문자열로 직렬화
        String jsonPayload = objectMapper.writeValueAsString(payload);
        // 4) 이스케이프 처리 (문자열 안에 문자열)
        String escapedPayload = objectMapper.writeValueAsString(jsonPayload);

        // 5) HTML + postMessage 스크립트 생성
        String html = ""
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "  <head>\n"
                + "    <meta charset=\"UTF-8\" />\n"
                + "    <title>Redirecting...</title>\n"
                + "  </head>\n"
                + "  <body>\n"
                + "    <script>\n"
                + "      window.ReactNativeWebView.postMessage(" + escapedPayload + ");\n"
                + "    </script>\n"
                + "  </body>\n"
                + "</html>";

        // 6) HTML 응답
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }
}
