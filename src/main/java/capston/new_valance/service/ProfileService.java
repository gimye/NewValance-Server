package capston.new_valance.service;

import capston.new_valance.dto.PreferredKeywordsDto;
import capston.new_valance.dto.req.ProfilePatchRequest;
import capston.new_valance.dto.res.DailyWatchCountResponse;
import capston.new_valance.dto.res.ProfileResponse;
import capston.new_valance.model.Tag;
import capston.new_valance.model.User;
import capston.new_valance.repository.TagRepository;
import capston.new_valance.repository.UserRepository;
import capston.new_valance.repository.UserTopTagRepository;
import capston.new_valance.repository.UserVideoInteractionRepository;
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

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfilePatchRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (req.getUsername() != null && !req.getUsername().equals(user.getUsername())) {
            userRepository.findByUsername(req.getUsername())
                    .ifPresent(u -> { throw new ResponseStatusException(
                            HttpStatus.CONFLICT, "이미 사용 중인 사용자 이름입니다."); });
            user.changeUsername(req.getUsername());
        }

        if (req.getProfileImage() != null && !req.getProfileImage().isEmpty()) {
            if (!req.getProfileImage().getContentType().startsWith("image/")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다.");
            }
            try {
                String url = s3Uploader.upload(req.getProfileImage(), "profile");
                user.changeProfilePicture(url);
            } catch (IOException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", e);
            }
        }

        return buildProfileResponse(user);
    }

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return buildProfileResponse(user);
    }

    private ProfileResponse buildProfileResponse(User user) {
        Long uid = user.getUserId();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay   = today.atTime(LocalTime.MAX);

        long todayViews = interactionRepository
                .countByUserIdAndWatchedAtBetween(uid, startOfDay, endOfDay);
        long totalViews = interactionRepository.countByUserId(uid);

        List<PreferredKeywordsDto> preferredKeywords =
                userTopTagRepository.findTop5ByUserIdOrderByWeightDesc(uid)
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

    @Transactional(readOnly = true)
    public List<List<DailyWatchCountResponse>> getWeeklyWatchCounts(Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<List<DailyWatchCountResponse>> allWeeks = new ArrayList<>(5);

        for (int w = 0; w < 5; w++) {
            List<DailyWatchCountResponse> week = new ArrayList<>(7);
            for (int d = 0; d < 7; d++) {
                LocalDate date = today.minusDays((long)w * 7 + d);
                LocalDateTime s = date.atStartOfDay();
                LocalDateTime e = date.atTime(LocalTime.MAX);

                int cnt = interactionRepository
                        .countByUserIdAndWatchedAtBetween(userId, s, e);
                week.add(DailyWatchCountResponse.builder()
                        .date(date.toString())
                        .value(cnt)
                        .build());
            }
            allWeeks.add(week);
        }
        return allWeeks;
    }
}