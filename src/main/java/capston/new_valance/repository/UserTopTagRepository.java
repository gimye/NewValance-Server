package capston.new_valance.repository;

import capston.new_valance.model.UserTopTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTopTagRepository extends JpaRepository<UserTopTag, Integer> {

    void deleteByUserId(Long userId);

    List<UserTopTag> findByUserId(Long userId);

    Optional<UserTopTag> findByUserIdAndTagId(Long userId, int tagId);

    // ✅ 추천 알고리즘용 상위 5개 태그 조회
    List<UserTopTag> findTop5ByUserIdOrderByWeightDesc(Long userId);
}
