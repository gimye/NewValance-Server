package capston.new_valance.repository;

import capston.new_valance.model.VideoVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoVersionRepository extends JpaRepository<VideoVersion, Long> {

    // article id로 videoversion 검색
    List<VideoVersion> findByArticle_ArticleIdOrderByVersionNameAsc(Long articleId);

    // 오늘 생성된 영상 전체를 생성일시 내림차순으로 조회
    List<VideoVersion> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end);
}
