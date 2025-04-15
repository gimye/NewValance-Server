package capston.new_valance.repository;

import capston.new_valance.model.UserTopTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTopTagRepository extends JpaRepository<UserTopTag, Integer> {
    void deleteByUserId(Long userId); // 사용자 ID로 기존 태그 삭제

    List<UserTopTag> findByUserId(Long userId);
}
