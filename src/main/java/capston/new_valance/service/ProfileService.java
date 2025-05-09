package capston.new_valance.service;

import capston.new_valance.dto.PreferredKeywordsDto;
import capston.new_valance.dto.req.ProfilePatchRequest;
import capston.new_valance.dto.res.DailyWatchCountResponse;
import capston.new_valance.dto.res.ProfileResponse;
import capston.new_valance.model.Tag;
import capston.new_valance.model.User;
import capston.new_valance.repository.*;
import capston.new_valance.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final UserVideoInteractionRepository interactionRepository;
    private final UserTopTagRepository userTopTagRepository;
    private final TagRepository tagRepository;
    private final S3Uploader s3Uploader;

    /* ===================================================================
       1) 조회: GET /api/profile
       =================================================================== */
    public ProfileResponse getProfile(Long userId) {
        return buildProfileResponse(userId);
    }

    /* ===================================================================
       2) 수정: PATCH /api/profile  (username · profileImage)
       =================================================================== */
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfilePatchRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "사용자를 찾을 수 없습니다."));

        /* 2-1) username 변경 (중복 체크) */
        if (req.getUsername() != null && !req.getUsername().equals(user.getUsername())) {
            userRepository.findByUsername(req.getUsername()).ifPresent(u -> {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "이미 사용 중인 사용자 이름입니다.");
            });
            user = user.toBuilder().username(req.getUsername()).build();
        }

        /* 2-2) 프로필 이미지 업로드 → S3 URL 저장 */
        if (req.getProfileImage() != null && !req.getProfileImage().isEmpty()) {

            if (!req.getProfileImage().getContentType().startsWith("image/")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다.");
            }

            try {
                String url = s3Uploader.upload(req.getProfileImage(), "profile");
                user = user.toBuilder().profilePictureUrl(url).build();
            } catch (IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", e);
            }
        }

        userRepository.save(user);

        /* 변경 완료 후 최신 프로필 반환 */
        return buildProfileResponse(userId);
    }

    /* ===================================================================
       3) 공통 빌더: 실제 ProfileResponse 생성 로직
       =================================================================== */
    private ProfileResponse buildProfileResponse(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        /* ── 오늘/총 시청 횟수 계산 ─────────────────────────────── */
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        long todayViews = interactionRepository
                .countByUserIdAndWatchedAtBetween(userId, startOfDay, endOfDay);

        long totalViews = interactionRepository.countByUserId(userId);

        /* ── 선호 키워드 Top-5 구성 ─────────────────────────────── */
        List<PreferredKeywordsDto> preferredKeywords =
                userTopTagRepository.findTop5ByUserIdOrderByWeightDesc(userId)
                        .stream()
                        .map(utt -> {
                            Tag tag = tagRepository.findById(utt.getTagId()).orElse(null);
                            return PreferredKeywordsDto.builder()
                                    .keyword(tag != null ? tag.getTagName() : "")
                                    .weight((long) Math.round(utt.getWeight()))
                                    .build();
                        })
                        .collect(Collectors.toList());

        /* ── DTO 조립 후 반환 ──────────────────────────────────── */
        return ProfileResponse.builder()
                .username(user.getUsername())
                .profileImgUrl(user.getProfilePictureUrl())
                .todayViews(todayViews)
                .totalViews(totalViews)
                .preferredKeywords(preferredKeywords)
                .build();
    }

    @Transactional(readOnly = true)
    public List<List<DailyWatchCountResponse>> getWeeklyWatchCounts(Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 1) 5주치(각 7일) 모두 채워둔 뒤
        List<List<DailyWatchCountResponse>> allWeeks = new ArrayList<>(5);
        for (int week = 0; week < 5; week++) {
            List<DailyWatchCountResponse> weekCounts = new ArrayList<>(7);
            for (int day = 0; day < 7; day++) {
                LocalDate date = today.minusDays((long) week * 7 + day);
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end   = date.atTime(LocalTime.MAX);

                int count = interactionRepository
                        .countByUserIdAndWatchedAtBetween(userId, start, end);

                weekCounts.add(DailyWatchCountResponse.builder()
                        .date(date.toString())
                        .value(count)
                        .build());
            }
            allWeeks.add(weekCounts);
        }

        // 2) “값(value) > 0”인 주(week)만 남기기
        List<List<DailyWatchCountResponse>> filtered = allWeeks.stream()
                .filter(week -> week.stream().anyMatch(d -> d.getValue() > 0))
                .collect(Collectors.toList());

        // 3) 아예 기록이 없으면 빈 배열 반환
        return filtered;
    }

}
