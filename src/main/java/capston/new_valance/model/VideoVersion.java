package capston.new_valance.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Entity
@Table(name = "videoversions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;

    @Column(nullable = false, length = 100)
    private String versionName;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public VideoVersion(NewsArticle article, String versionName, String videoUrl) {
        this.article = article;
        this.versionName = versionName;
        this.videoUrl = videoUrl;
        this.createdAt = LocalDateTime.now();
    }

    // ✅ 테스트용 정적 팩토리 메서드
    public static VideoVersion testInstance(Long videoId, String versionName, String videoUrl) {
        VideoVersion video = new VideoVersion();
        try {
            Field idField = VideoVersion.class.getDeclaredField("videoId");
            idField.setAccessible(true);
            idField.set(video, videoId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set videoId for test instance", e);
        }
        video.versionName = versionName;
        video.videoUrl = videoUrl;
        video.createdAt = LocalDateTime.now();
        return video;
    }
}
