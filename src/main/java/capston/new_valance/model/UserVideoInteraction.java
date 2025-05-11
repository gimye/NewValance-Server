package capston.new_valance.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "uservideointeractions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserVideoInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionId;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private NewsArticle article;

    private LocalDateTime watchedAt;
    private int watchDuration;
    @Getter
    private boolean liked;


    public static UserVideoInteraction ofNewInteraction(Long uid, NewsArticle art) {
        return UserVideoInteraction.builder()
                .userId(uid)
                .article(art)
                .watchedAt(LocalDateTime.now())
                .watchDuration(0)
                .liked(false)
                .build();
    }

    public boolean isSameArticle(Long articleId) {
        return this.article != null && this.article.getArticleId().equals(articleId);
    }

    public UserVideoInteraction markLiked()   { return this.toBuilder().liked(true).build(); }
    public UserVideoInteraction unmarkLiked() { return this.toBuilder().liked(false).build(); }
}