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

        String token = resolveToken(request);
        System.out.println("🟡 요청 URL: " + request.getRequestURI());
        System.out.println("🟡 추출된 토큰: " + token);

        if (token != null && jwtUtil.validateToken(token)) {
            try {
                Claims claims = jwtUtil.extractClaims(token);
                System.out.println("✅ JwtFilter 동작함");
                System.out.println("✅ userId: " + claims.get("userId"));
                System.out.println("✅ provider: " + claims.get("provider"));

                // 인증 객체 생성
                UserPrincipal principal = new UserPrincipal(
                        claims.get("userId", Long.class),
                        claims.get("username", String.class),
                        claims.get("email", String.class),
                        LoginProvider.valueOf(claims.get("provider", String.class))
                );

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                throw new RuntimeException("Invalid JWT Token", e);
            }
        } else {
            System.out.println("⚠️ 토큰이 없거나 유효하지 않음");
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
