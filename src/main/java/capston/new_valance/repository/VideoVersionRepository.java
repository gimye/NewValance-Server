package capston.new_valance.repository;

import capston.new_valance.model.VideoVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface VideoVersionRepository extends JpaRepository<VideoVersion, Long> {

    // 이미 있는 메서드일 수 있음
    List<VideoVersion> findByArticle_ArticleIdOrderByVersionNameAsc(Long articleId);

    // 추가해줘야 할 메서드
    Optional<VideoVersion> findTopByArticle_ArticleIdOrderByVersionNameAsc(Long articleId);
}
