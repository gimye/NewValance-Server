package capston.new_valance.repository;

import capston.new_valance.model.UserVideoInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserVideoInteractionRepository extends JpaRepository<UserVideoInteraction, Long> {

    // 특정 사용자가 특정 기사에 대해 어떤 상호작용이라도 한 적이 있는지 확인
    boolean existsByUserIdAndArticle_ArticleId(Long userId, Long articleId);

    // 특정 사용자가 특정 기사에 대해 좋아요(liked=true)를 눌렀는지 확인
    boolean existsByUserIdAndArticle_ArticleIdAndLikedTrue(Long userId, Long articleId);

    // 특정 사용자가 특정 기사에 대해 남긴 상호작용 기록 조회
    Optional<UserVideoInteraction> findByUserIdAndArticle_ArticleId(Long userId, Long articleId);

    // 특정 사용자의 최근 시청 상호작용 100개를 최신순으로 조회
    List<UserVideoInteraction> findTop100ByUserIdOrderByWatchedAtDesc(Long userId);

    // 특정 사용자의 주어진 기간(start~end) 내 시청 횟수 집계
    int countByUserIdAndWatchedAtBetween(Long userId,
                                         LocalDateTime start,
                                         LocalDateTime end);

    // 특정 사용자의 총 시청 횟수 집계
    long countByUserId(Long userId);

    // 특정 사용자가 좋아요(liked=true) 표시한 모든 상호작용을 최신순으로 조회
    List<UserVideoInteraction> findByUserIdAndLikedTrueOrderByWatchedAtDesc(Long userId);

    // 특정 사용자가 좋아요(liked=true) 표시한 상호작용을 페이징 처리하여 최신순으로 조회
    Page<UserVideoInteraction> findByUserIdAndLikedTrueOrderByWatchedAtDesc(Long userId, Pageable pageable);
}
