package capston.new_valance.repository;

import capston.new_valance.model.LoginProvider;
import capston.new_valance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 공급자까지 포함한 조회 메서드
    Optional<User> findByEmailAndLoginProvider(String email, LoginProvider loginProvider);

    // 이메일만으로 조회 (중복 체크에 사용)
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
