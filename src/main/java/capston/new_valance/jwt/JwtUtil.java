package capston.new_valance.jwt;

import capston.new_valance.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessExpireMs = 86400000; // 24시간

    // JwtUtil에서 사용할 Secret Key 받아오기
    public JwtUtil(@Value("${SECRET_KEY}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // 토큰 생성 (카카오 사용자 정보 기반)
    public String generateToken(User user) {
        JwtBuilder builder = Jwts.builder()
                .claim("email", user.getEmail())
                .claim("userId", user.getUserId());

        // 닉네임 존재 시에만 클레임 추가
        if (user.getUsername() != null) {
            builder.claim("name", user.getUsername());
        }

        // 프로필 사진 존재 시에만 클레임 추가
        if (user.getProfilePictureUrl() != null) {
            builder.claim("picture", user.getProfilePictureUrl());
        }

        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .claim("userId", user.getUserId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 604800000)) // 7일
                .signWith(secretKey)
                .compact();
    }


    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // 클레임 추출
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 사용자 ID 추출
    public Long getUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }
}
