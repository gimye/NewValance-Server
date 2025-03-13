package capston.new_valance.jwt;

import capston.new_valance.model.LoginProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization header에서 JWT 추출
        String token = resolveToken(request);

        // token 유효성 체크
        if (token != null && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.extractClaims(token);

            // 인증 객체 생성
            UserPrincipal principal = new UserPrincipal(
                    claims.get("userId", Long.class),
                    claims.get("username", String.class),
                    claims.get("email", String.class),
                    LoginProvider.valueOf(claims.get("provider", String.class))
            );

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    principal, null, Collections.emptyList());

            // SecurityContext에 사용자 정보 등록
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        return (bearerToken != null && bearerToken.startsWith("Bearer "))
                ? bearerToken.substring(7)
                : null;
    }
}
