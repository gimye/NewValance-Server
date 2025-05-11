package capston.new_valance.repository;

import capston.new_valance.model.LoginProvider;
import capston.new_valance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // email, login provider로 사용자 조회
    Optional<User> findByEmailAndLoginProvider(String email, LoginProvider loginProvider);

    // email로 사용자 조회
    Optional<User> findByEmail(String email);

    // username으로 사용자 조회
    Optional<User> findByUsername(String username);
}
