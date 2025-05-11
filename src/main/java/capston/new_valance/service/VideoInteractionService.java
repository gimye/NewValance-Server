package capston.new_valance.service;

import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.UserVideoInteraction;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.UserVideoInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class VideoInteractionService {

    private final UserVideoInteractionRepository interactionRepo;
    private final NewsArticleRepository articleRepo;

    /**
     * 뉴스 영상 시청 완료 처리
     * - 한국(Asia/Seoul) 기준 현재 시각으로 watchedAt 저장
     */
    @Transactional
    public int handleArticleComplete(Long userId, Long articleId) {
        NewsArticle article = articleRepo.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스가 존재하지 않습니다."));

        // 이미 본 영상은 건너뛰기
        if (interactionRepo.existsByUserIdAndArticle_ArticleId(userId, articleId)) {
            return getTodayViewCount(userId);
        }

        // 새로운 시청 기록 저장
        interactionRepo.save(UserVideoInteraction.ofNewInteraction(userId, article));
        return getTodayViewCount(userId);
    }

    /**
     * 오늘(Asia/Seoul 00:00~익일 00:00) 시청한 영상 수 조회
     */
    @Transactional(readOnly = true)
    public int getTodayViewCount(Long userId) {
        LocalDate todaySeoul = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime start = todaySeoul.atStartOfDay();
        LocalDateTime end   = todaySeoul.plusDays(1).atStartOfDay();

        return interactionRepo.countByUserIdAndWatchedAtBetween(userId, start, end);
    }
}