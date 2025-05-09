/* ============================================================= */
package capston.new_valance.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;
    private final String bucket = "newvalance";  // yml에 넣고 @Value로 받아도 됨

    /** 파일 업로드 후 공개 URL 반환 */
    public String upload(MultipartFile file, String dir) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : "";
        String key = "%s/%s%s".formatted(dir, UUID.randomUUID(), ext);

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(file.getContentType());
        meta.setContentLength(file.getSize());

        amazonS3.putObject(bucket, key, file.getInputStream(), meta);

        // public-read 권한 버킷이면 바로 접근 가능
        return amazonS3.getUrl(bucket, key).toString();
    }
}
