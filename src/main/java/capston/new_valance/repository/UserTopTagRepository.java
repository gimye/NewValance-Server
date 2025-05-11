package capston.new_valance.repository;

import capston.new_valance.model.UserTopTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTopTagRepository extends JpaRepository<UserTopTag, Integer> {

    // 사용자 id로 기록 삭제
    void deleteByUserId(Long userId);

    // user id로 사용자 태그 조회
    List<UserTopTag> findByUserId(Long userId);

    // user id와 tag id로 사용자 태그 선호도 조회
    Optional<UserTopTag> findByUserIdAndTagId(Long userId, int tagId);

    // 추천 알고리즘용 상위 5개 태그 조회
    List<UserTopTag> findTop5ByUserIdOrderByWeightDesc(Long userId);
}
