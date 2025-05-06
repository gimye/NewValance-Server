package capston.new_valance.service;

import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.UserVideoInteraction;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.UserVideoInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VideoInteractionService {

    private final UserVideoInteractionRepository interactionRepo;
    private final NewsArticleRepository articleRepo;

    /** 뉴스 기사 단위 시청 완료 처리 */
    public int handleArticleComplete(Long userId, Long articleId) {

        NewsArticle article = articleRepo.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스가 존재하지 않습니다."));

        /* 이미 본 기사면 저장 스킵 */
        if (interactionRepo.existsByUserIdAndArticle_ArticleId(userId, articleId)) {
            return getTodayViewCount(userId);
        }

        /* 새 시청 기록 저장 */
        interactionRepo.save(UserVideoInteraction.ofNewInteraction(userId, article));
        return getTodayViewCount(userId);
    }

    /** 오늘(00:00~24:00) 시청한 기사 수 */
    public int getTodayViewCount(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();  // 오늘 00:00
        LocalDateTime end   = start.plusDays(1);               // 내일 00:00

        return interactionRepo.countByUserIdAndWatchedAtBetween(userId, start, end);
    }
}
