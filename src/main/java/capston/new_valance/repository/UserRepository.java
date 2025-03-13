package capston.new_valance.repository;

import capston.new_valance.model.LoginProvider;
import capston.new_valance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndLoginProvider(String email, LoginProvider loginProvider);
    boolean existsByEmail(String email);
}
