package capston.new_valance.model;

import capston.new_valance.oauth2.OAuth2Response;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_email_provider",
                columnNames = {"email", "login_provider"}
        )
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "login_provider")
    private LoginProvider loginProvider;



    // 빌더 생성 메서드 (필수 필드 강제)
    public static User.UserBuilder builder(String email, LoginProvider loginProvider) {
        return new User.UserBuilder()
                .email(email)
                .loginProvider(loginProvider);
    }

    // 기존 static factory method 제거 → 빌더 패턴으로 대체
}
