package capston.new_valance.oauth2;

import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public CustomSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = customOAuth2User.getUser();

        // 토큰 생성
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);  // 새로 추가

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{"
                + "\"access_token\":\"" + accessToken + "\","
                + "\"refresh_token\":\"" + refreshToken + "\","
                + "\"message\":\"로그인 성공\""
                + "}");
        response.getWriter().flush();
    }
}

