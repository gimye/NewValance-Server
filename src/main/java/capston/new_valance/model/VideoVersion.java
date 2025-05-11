package capston.new_valance.model;

import jakarta.persistence.*;
import lombok.*;

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

}
