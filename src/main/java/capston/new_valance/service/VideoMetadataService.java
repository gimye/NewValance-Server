package capston.new_valance.service;

import capston.new_valance.dto.VideoVersionDto;
import capston.new_valance.dto.TagDto;
import capston.new_valance.dto.req.VideoMetadataRequest;
import capston.new_valance.dto.res.VideoMetadataResponse;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.Tag;
import capston.new_valance.model.VideoVersion;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.TagRepository;
import capston.new_valance.repository.VideoVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoMetadataService {

    private final NewsArticleRepository articleRepo;
    private final VideoVersionRepository videoRepo;
    private final TagRepository tagRepo;

    @Transactional
    public VideoMetadataResponse saveMetadata(VideoMetadataRequest req) {
        LocalDateTime publishedAt = parseDateTime(req.getPublishedAt());
        LocalDateTime createdAt   = parseDateTime(req.getCreatedAt());

        // 1. 뉴스 기사 저장
        NewsArticle article = NewsArticle.builder()
                .title(req.getTitle())
                .categoryId(req.getCategoryId())
                .originalUrl(req.getOriginalUrl())
                .publishedAt(publishedAt)
                .thumbnailUrl(req.getThumbnailUrl())
                .createdAt(createdAt)
                .build();
        article = articleRepo.save(article);

        // 2. 영상 버전 저장
        List<VideoVersion> savedVersions = videoRepo.saveAll(Arrays.asList(
                new VideoVersion(article, "easy", req.getEasyVersionUrl()),
                new VideoVersion(article, "normal", req.getNormalVersionUrl())
        ));

        // 3. 태그 등록 및 연결
        Set<Tag> tagSet = Arrays.stream(req.getTags().split(","))
                .map(String::trim)
                .filter(tagName -> !tagName.isEmpty())
                .map(tagName -> tagRepo.findByTagName(tagName)
                        .orElseGet(() -> tagRepo.save(Tag.builder().tagName(tagName).build())))
                .collect(Collectors.toSet());

        article.getTags().addAll(tagSet);
        articleRepo.save(article); // 연관 테이블 저장

        // 4. 응답 DTO 구성
        List<VideoVersionDto> versionDtos = savedVersions.stream()
                .map(v -> VideoVersionDto.builder()
                        .versionName(v.getVersionName())
                        .videoUrl(v.getVideoUrl())
                        .build())
                .toList();

        List<TagDto> tagDtos = tagSet.stream()
                .map(tag -> TagDto.builder()
                        .tagId((long) tag.getTagId())
                        .tagName(tag.getTagName())
                        .build())
                .toList();

        return VideoMetadataResponse.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .categoryId(article.getCategoryId())
                .originalUrl(article.getOriginalUrl())
                .publishedAt(article.getPublishedAt().toString())
                .createdAt(article.getCreatedAt().toString())
                .videoVersions(versionDtos)
                .tags(tagDtos)
                .build();
    }

    /** 문자열이 "yyyy-MM-dd'T'HH-mm-ss" 형식이면 HH,mm,ss 사이 구분자를 ':' 로 교체 후 파싱 */

    private LocalDateTime parseDateTime(String raw) {
        String normalized = normalizeTimeDelimiters(raw);
        return LocalDateTime.parse(normalized); // ISO_LOCAL_DATE_TIME
    }

    private String normalizeTimeDelimiters(String raw) {
        int t = raw.indexOf('T');
        if (t > 0 && raw.length() >= t + 9) {
            // datePart: "yyyy-MM-ddT" / timePart: "HH-mm-ss" → "HH:mm:ss"
            String datePart = raw.substring(0, t + 1);
            String timePart = raw.substring(t + 1).replace('-', ':');
            return datePart + timePart;
        }
        return raw;
    }
}
