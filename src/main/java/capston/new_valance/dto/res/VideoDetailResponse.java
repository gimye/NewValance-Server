package capston.new_valance.dto.res;

public record VideoDetailResponse(
        String title,
        String originalUrl,
        String normalVideoUrl,
        String easyVideoUrl,
        Long nextNewsId
) {}
