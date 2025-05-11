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

    // 프로필 조회
    public ProfileResponse getProfile(Long userId) {
        return buildProfileResponse(userId);
    }

    // 프로필 수정
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfilePatchRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 사용자 이름 중복확인
        if (req.getUsername() != null && !req.getUsername().equals(user.getUsername())) {
            userRepository.findByUsername(req.getUsername()).ifPresent(u -> {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "이미 사용 중인 사용자 이름입니다.");
            });
            user = user.toBuilder().username(req.getUsername()).build();
        }

        // S3에 이미지 업로드
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

        // 변경 완료 후 최신 프로필 반환
        return buildProfileResponse(userId);
    }

    // profile response 생성 메서드
    private ProfileResponse buildProfileResponse(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 오늘&총 시청 횟수 계산
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        long todayViews = interactionRepository
                .countByUserIdAndWatchedAtBetween(userId, startOfDay, endOfDay);

        long totalViews = interactionRepository.countByUserId(userId);

        // 선호 키워드 Top-5 구성
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

        return ProfileResponse.builder()
                .username(user.getUsername())
                .profileImgUrl(user.getProfilePictureUrl())
                .todayViews(todayViews)
                .totalViews(totalViews)
                .preferredKeywords(preferredKeywords)
                .build();
    }

    // 최근 5주간 시청 기록 리스트 반환
    @Transactional(readOnly = true)
    public List<List<DailyWatchCountResponse>> getWeeklyWatchCounts(Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

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
        return allWeeks;
    }


}
