package capston.new_valance.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private boolean liked;

    /* --------- 팩토리 메서드 --------- */
    public static UserVideoInteraction ofNewInteraction(Long userId, NewsArticle article) {
        return UserVideoInteraction.builder()
                .userId(userId)
                .article(article)
                .watchedAt(LocalDateTime.now())
                .watchDuration(0)
                .liked(false)
                .build();
    }

    /* --------- 유틸 --------- */
    public boolean isSameArticle(Long articleId) {
        return this.article != null && this.article.getArticleId().equals(articleId);
    }

    public boolean isLiked() { return liked; }
    public UserVideoInteraction markLiked()   { return this.toBuilder().liked(true).build(); }
    public UserVideoInteraction unmarkLiked() { return this.toBuilder().liked(false).build(); }
}
