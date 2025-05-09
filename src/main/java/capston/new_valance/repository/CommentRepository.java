// src/main/java/capston/new_valance/repository/CommentRepository.java
package capston.new_valance.repository;

import capston.new_valance.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 지정된 article_id(뉴스) 에 달린 댓글을
     * 페이지 단위로 조회합니다.
     */
    Page<Comment> findByArticle_ArticleId(Long articleId, Pageable pageable);
}
