package capston.new_valance.repository;

import capston.new_valance.model.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    Page<NewsArticle> findByCategoryIdOrderByPublishedAtDesc(Long categoryId, Pageable pageable);
    List<NewsArticle> findTop10ByCategoryIdOrderByPublishedAtDesc(Long categoryId);
}
