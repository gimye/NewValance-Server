package capston.new_valance.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "videoversions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VideoVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long videoId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;

    @Column(nullable = false, length = 100)
    private String versionName;

    @Column(nullable = false)
    private String videoUrl;

    @Column
    private String thumbnailUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}

