package capston.new_valance.service;

import capston.new_valance.dto.NewsSimpleDto;
import capston.new_valance.dto.req.OnboardingRequest;
import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.model.User;
import capston.new_valance.model.UserTopTag;
import capston.new_valance.model.Tag;
import capston.new_valance.repository.UserRepository;
import capston.new_valance.repository.UserTopTagRepository;
import capston.new_valance.repository.TagRepository;
import capston.new_valance.repository.UserVideoInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final JwtUtil jwtUtil;
    private final UserTopTagRepository userTopTagRepository;
    private final UserVideoInteractionRepository interactionRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

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

    @Transactional
    public void updateUserTags(String accessToken, List<String> tagNames) {
        String token = accessToken.replace("Bearer ", "");
        Long userId = jwtUtil.getUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 기존 태그 삭제
        userTopTagRepository.deleteByUserId(userId);

        // 전달된 태그 이름으로 태그 엔티티를 조회 후 저장
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName));

            UserTopTag userTopTag = UserTopTag.builder()
                    .userId(userId)
                    .tagId(tag.getTagId())
                    .weight(10f) // 기본 가중치 값
                    .lastUpdated(LocalDateTime.now())
                    .build();

            userTopTagRepository.save(userTopTag);
        }
    }

    // 사용자 선호 태그 목록을 반환하는 메서드
    public List<String> getUserPreferredTags(Long userId) {
        List<UserTopTag> userTopTags = userTopTagRepository.findByUserId(userId);
        return userTopTags.stream()
                .map(userTopTag -> {
                    Tag tag = tagRepository.findById(userTopTag.getTagId())
                            .orElse(null);
                    return tag != null ? tag.getTagName() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Page<NewsSimpleDto> getLikedNews(Long userId, Pageable pageable) {
        return interactionRepository
                .findByUserIdAndLikedTrueOrderByWatchedAtDesc(userId, pageable)
                .map(interaction -> NewsSimpleDto.builder()
                        .articleId(interaction.getArticle().getArticleId())
                        .title(interaction.getArticle().getTitle())
                        .thumbnailUrl(interaction.getArticle().getThumbnailUrl())
                        .build()
                );
    }

}
