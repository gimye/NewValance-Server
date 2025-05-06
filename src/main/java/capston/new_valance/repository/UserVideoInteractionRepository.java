package capston.new_valance.repository;

import capston.new_valance.model.UserVideoInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface UserVideoInteractionRepository extends JpaRepository<UserVideoInteraction, Long> {

    /* ------------------------------------------------------------
       기사·좋아요 관련 메서드 ― 메서드 이름 쿼리이므로 그대로 사용
       ------------------------------------------------------------ */
    boolean existsByUserIdAndArticle_ArticleId(Long userId, Long articleId);

    boolean existsByUserIdAndArticle_ArticleIdAndLikedTrue(Long userId, Long articleId);

    Optional<UserVideoInteraction> findByUserIdAndArticle_ArticleId(Long userId, Long articleId);

    List<UserVideoInteraction> findTop100ByUserIdOrderByWatchedAtDesc(Long userId);

    /* ------------------------------------------------------------
       오늘 시청 횟수 카운트 ― JPQL 함수 없이 안전하게
       ------------------------------------------------------------ */
    int countByUserIdAndWatchedAtBetween(Long userId,
                                         LocalDateTime start,
                                         LocalDateTime end);
}
