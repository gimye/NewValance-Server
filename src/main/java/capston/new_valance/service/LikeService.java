package capston.new_valance.service;

import capston.new_valance.model.*;
import capston.new_valance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final NewsArticleRepository newsArticleRepository;
    private final UserVideoInteractionRepository interactionRepository;
    private final TagRepository tagRepository;
    private final UserTopTagRepository userTopTagRepository;

    // 좋아요 처리
    @Transactional
    public boolean toggleLike(Long articleId, Long userId) {

        NewsArticle article = newsArticleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스가 존재하지 않습니다."));

        Optional<UserVideoInteraction> opt =
                interactionRepository.findByUserIdAndArticle_ArticleId(userId, articleId);

        boolean willLike;
        if (opt.isPresent()) {
            UserVideoInteraction cur = opt.get();
            willLike = !cur.isLiked();
            interactionRepository.save(cur.toBuilder().liked(willLike).build());
        } else {
            willLike = true;
            interactionRepository.save(
                    UserVideoInteraction.ofNewInteraction(userId, article).markLiked()
            );
        }

        /* ----------------- 태그 가중치 반영 ----------------- */
        article.getTags().forEach(tag -> {
            Optional<UserTopTag> tagOpt =
                    userTopTagRepository.findByUserIdAndTagId(userId, tag.getTagId());

            if (willLike) {                         // 좋아요 추가
                UserTopTag t = tagOpt.orElseGet(() ->
                        UserTopTag.builder()
                                .userId(userId)
                                .tagId(tag.getTagId())
                                .weight(0f)
                                .build());
                t.increaseWeight(1f);
                userTopTagRepository.save(t);
            } else {                               // 좋아요 취소
                tagOpt.ifPresent(t -> {
                    t.decreaseWeight(1f);
                    if (t.getWeight() <= 0) userTopTagRepository.delete(t);
                    else userTopTagRepository.save(t);
                });
            }
        });

        return willLike;
    }
}
