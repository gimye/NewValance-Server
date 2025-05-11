package capston.new_valance.repository;

import capston.new_valance.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    // 태그 이름으로 태그 조회
    Optional<Tag> findByTagName(String tagName);
}
