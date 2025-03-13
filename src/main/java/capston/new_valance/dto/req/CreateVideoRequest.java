package capston.new_valance.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateVideoRequest {

    // 동영상 제목
    private String title;

    // 뉴스 카테고리 id
    private Long categoryId;

    // 원문 뉴스기사 url
    private String originalUrl;

    // 원문 뉴스기사 작성일시
    private LocalDateTime publishedAt;

    // 영상 생성일시
    private LocalDateTime createdAt;

    // 버전별 S3 URL
    private String easyVersionUrl;
    private String normalVersionUrl;

    // 영상 썸네일 이미지 URL
    private String thumbnailUrl;

    // 영상 tag 문자열 ex) "국회,선거,정부,법원,대통령,진보"
    private String tags;
}
