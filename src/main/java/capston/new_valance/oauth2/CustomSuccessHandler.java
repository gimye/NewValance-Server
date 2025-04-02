package capston.new_valance.oauth2;

import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.model.User;

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

        // [핵심 로직] 닉네임 또는 프로필 사진이 없는 경우 신규 회원으로 판단
        boolean isNew =
                user.getUsername() == null ||
                        user.getUsername().isBlank() ||
                        user.getProfilePictureUrl() == null ||
                        user.getProfilePictureUrl().isBlank();

        // 토큰 생성
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // JSON 응답 구성
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{"
                + "\"access_token\":\"" + accessToken + "\","
                + "\"refresh_token\":\"" + refreshToken + "\","
                + "\"isNew\":" + isNew + ","
                + "\"user\":{"
                + "\"userId\":" + user.getUserId() + ","
                + "\"email\":\"" + user.getEmail() + "\","
                + "\"username\":" + (user.getUsername() != null ? "\"" + user.getUsername() + "\"" : "null") + ","
                + "\"profileImage\":" + (user.getProfilePictureUrl() != null ? "\"" + user.getProfilePictureUrl() + "\"" : "null")
                + "}"
                + "}");
    }

}
