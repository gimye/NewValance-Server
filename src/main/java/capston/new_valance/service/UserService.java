package capston.new_valance.service;

import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.model.LoginProvider;
import capston.new_valance.model.User;
import capston.new_valance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private final JwtUtil jwtUtil;

    @Transactional
    public void updateUsername(String accessToken, String newUsername) {
        String token = accessToken.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        User updatedUser = user.toBuilder()
                .username(newUsername)
                .build();

        userRepository.save(updatedUser);
    }

    public boolean isUsernameAvailable(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 사용자 이름입니다: " + username);
        }
        return true;
    }

}