package capston.new_valance.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
// User.java
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_email_provider",
                columnNames = {"email", "login_provider"}  // 복합 유니크 키
        )
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    private String username;

    @Column(nullable = false) // 🔥 이메일 필드 NOT NULL
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

    public static User createSocialUser(String email, LoginProvider provider) {
        User user = new User();
        user.setEmail(email);
        user.setLoginProvider(provider);
        return user;
    }
}
