package capston.new_valance.jwt;

import capston.new_valance.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessExpireMs = 86400000; // 24시간

    public JwtUtil(@Value("${SECRET_KEY}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // Access Token 생성
    public String generateToken(User user) {
        JwtBuilder builder = Jwts.builder()
                .claim("email", user.getEmail())
                .claim("userId", user.getUserId())
                .claim("provider", user.getLoginProvider().name());

        if (user.getUsername() != null) {
            builder.claim("username", user.getUsername());
        }

        if (user.getProfilePictureUrl() != null) {
            builder.claim("picture", user.getProfilePictureUrl());
        }

        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpireMs))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .claim("userId", user.getUserId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 604800000)) // 7일
                .signWith(secretKey)
                .compact();
    }

    // ✅ 유효성 검사 (최신 방식)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            log.info("✅ JWT 유효성 검사 통과");
            return true;
        } catch (Exception e) {
            log.error("❌ JWT 유효성 실패: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    // ✅ 클레임 추출 (이미 최신 방식으로 잘 되어 있음!)
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }
}
