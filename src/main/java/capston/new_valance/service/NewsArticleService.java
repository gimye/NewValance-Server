package capston.new_valance.service;

import capston.new_valance.dto.req.CreateVideoRequest;
import capston.new_valance.model.NewsArticle;
import capston.new_valance.model.VideoVersion;
import capston.new_valance.model.Tag;
import capston.new_valance.repository.NewsArticleRepository;
import capston.new_valance.repository.VideoVersionRepository;
import capston.new_valance.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final VideoVersionRepository videoVersionRepository;
    private final TagRepository tagRepository;

    @Transactional
    public NewsArticle createNewsArticle(CreateVideoRequest request) {
        NewsArticle article = new NewsArticle();
        article.setTitle(request.getTitle());
        article.setCategoryId(request.getCategoryId());
        article.setOriginalUrl(request.getOriginalUrl());
        article.setPublishedAt(request.getPublishedAt());
        article.setCreatedAt(request.getCreatedAt());

        // VideoVersion 생성 및 연결
        List<VideoVersion> versions = new ArrayList<>();
        versions.add(createVideoVersion("easy", request.getEasyVersionUrl(), article, request.getThumbnailUrl()));
        versions.add(createVideoVersion("normal", request.getNormalVersionUrl(), article, request.getThumbnailUrl()));
        article.setVideoVersions(versions);

        Set<Tag> tags = processTagsAndGetTagSet(request.getTags());
        article.setTags(tags);

        return newsArticleRepository.save(article);
    }

    private VideoVersion createVideoVersion(String versionName, String url, NewsArticle article, String thumbnailUrl) {
        VideoVersion version = new VideoVersion();
        version.setVersionName(versionName);
        version.setVideoUrl(url);
        version.setArticle(article);
        version.setThumbnailUrl(thumbnailUrl);
        version.setCreatedAt(article.getCreatedAt());
        return version;
    }

    private Set<Tag> processTagsAndGetTagSet(String tagString) {
        Set<Tag> tagSet = new HashSet<>();
        String[] tagNames = tagString.split(",");
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setTagName(tagName);
                        return tagRepository.save(newTag);
                    });
            tagSet.add(tag);
        }
        return tagSet;
    }
}