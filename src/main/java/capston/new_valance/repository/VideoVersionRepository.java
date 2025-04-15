package capston.new_valance.repository;

import capston.new_valance.model.VideoVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoVersionRepository extends JpaRepository<VideoVersion, Long> {
    // 뉴스 기사(articleId) 별 영상 버전을 versionName 오름차순으로 조회
    List<VideoVersion> findByArticle_ArticleIdOrderByVersionNameAsc(Long articleId);
}
