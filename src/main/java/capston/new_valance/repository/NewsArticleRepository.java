package capston.new_valance.repository;

import capston.new_valance.model.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    // 홈 가판대, 카테고리별 뉴스 조회용 - 최신 뉴스 10개 (기존 기능)
    List<NewsArticle> findTop10ByCategoryIdOrderByPublishedAtDesc(Long categoryId);

    // 페이징 조회용
    Page<NewsArticle> findByCategoryId(Long categoryId, Pageable pageable);

    // 단일 뉴스 조회
    Optional<NewsArticle> findByArticleId(Long articleId);

    // banner를 위한 임시 메서드
    List<NewsArticle> findTop3ByOrderByPublishedAtDescArticleIdAsc();

    // === 영상 API 전용 ===

    // newsId 없이 카테고리의 최신 뉴스 3개 조회 (publishedAt 내림차순, 동일 시 articleId 오름차순)
    // (메서드명만으로 조회 제한이 작동하도록 @Query 없이 작성)
    List<NewsArticle> findTop3ByCategoryIdOrderByPublishedAtDescArticleIdAsc(Long categoryId);

    // newsId가 주어졌을 때, 기준 뉴스 이후 조건에 부합하는 3개 뉴스 조회
    // 조건: 기준 뉴스의 publishedAt 보다 이전이거나, publishedAt이 동일하면 articleId가 큰 뉴스
    // native query를 사용하여 LIMIT 3을 직접 적용함
    @Query(value = "SELECT * FROM newsarticles n " +
            "WHERE n.category_id = :categoryId " +
            "AND ( n.published_at < (SELECT published_at FROM newsarticles WHERE article_id = :newsId) " +
            "   OR (n.published_at = (SELECT published_at FROM newsarticles WHERE article_id = :newsId) " +
            "       AND n.article_id >= :newsId) ) " +
            "ORDER BY n.published_at DESC, n.article_id ASC " +
            "LIMIT 3", nativeQuery = true)
    List<NewsArticle> findNext3ByCategoryIdAndNewsId(@Param("categoryId") Long categoryId, @Param("newsId") Long newsId);


    // 다음 뉴스 id 조회 (현재 응답 뉴스의 마지막 뉴스 이후에 해당하는 뉴스가 있으면 그 articleId 반환)
    // native query를 사용하여 한 건만 반환(LIMIT 1)하도록 함
    @Query(value = "SELECT n.article_id FROM newsarticles n " +
            "WHERE n.category_id = :categoryId " +
            "AND ( n.published_at < (SELECT published_at FROM newsarticles WHERE article_id = :currentId) " +
            "      OR (n.published_at = (SELECT published_at FROM newsarticles WHERE article_id = :currentId) " +
            "          AND n.article_id > :currentId) ) " +
            "ORDER BY n.published_at DESC, n.article_id ASC " +
            "LIMIT 1", nativeQuery = true)
    Optional<Long> findNextNewsId(@Param("currentId") Long currentId, @Param("categoryId") Long categoryId);

    @Query(value = """
    SELECT a.*
    FROM newsarticles a
    JOIN article_tags at ON a.article_id = at.article_id
    WHERE at.tag_id IN :tagIds
    GROUP BY a.article_id
    ORDER BY COUNT(at.tag_id) DESC, a.published_at DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<NewsArticle> findRecommendedArticlesByTagIds(
            @Param("tagIds") List<Integer> tagIds,
            @Param("limit") int limit
    );

}
