package capston.new_valance.repository;

import capston.new_valance.model.UserVideoInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserVideoInteractionRepository extends JpaRepository<UserVideoInteraction, Long> {

    boolean existsByUserIdAndArticle_ArticleId(Long userId, Long articleId);
    boolean existsByUserIdAndArticle_ArticleIdAndLikedTrue(Long userId, Long articleId);
    Optional<UserVideoInteraction> findByUserIdAndArticle_ArticleId(Long userId, Long articleId);
    List<UserVideoInteraction> findTop100ByUserIdOrderByWatchedAtDesc(Long userId);

    int  countByUserIdAndWatchedAtBetween(Long userId,
                                          LocalDateTime start,
                                          LocalDateTime end);

    long countByUserId(Long userId);   // 총 시청 수
    List<UserVideoInteraction> findByUserIdAndLikedTrueOrderByWatchedAtDesc(Long userId);
    Page<UserVideoInteraction> findByUserIdAndLikedTrueOrderByWatchedAtDesc(Long userId, Pageable pageable);
}
