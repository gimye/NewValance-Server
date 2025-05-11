package capston.new_valance.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final AmazonS3 amazonS3;
    private final String bucket = "newvalance";

    public String upload(MultipartFile file, String dir) throws IOException {

        String ext = (file.getOriginalFilename() != null
                && file.getOriginalFilename().contains("."))
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'))
                : "";
        String key = String.format("%s/%s%s", dir, UUID.randomUUID(), ext);

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(file.getContentType());
        meta.setContentLength(file.getSize());

        amazonS3.putObject(bucket, key, file.getInputStream(), meta);

        String url = amazonS3.getUrl(bucket, key).toString();
        return url;
    }
}
