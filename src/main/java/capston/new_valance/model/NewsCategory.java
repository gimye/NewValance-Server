package capston.new_valance.model;

import lombok.Getter;

import java.util.Map;

public enum NewsCategory {
    POLITICS(1L, "정치", "politics"),
    ECONOMY(2L, "경제", "economy"),
    INTERNATIONAL(3L, "국제", "international"),
    CULTURE(4L, "문화", "culture"),
    SOCIETY(5L, "사회", "society"),
    IT_SCIENCE(6L, "IT/과학", "it");

    @Getter
    private final Long id;
    private final String koreanName;
    private final String typeName;

    NewsCategory(Long id, String koreanName, String typeName) {
        this.id = id;
        this.koreanName = koreanName;
        this.typeName = typeName;
    }

    // URL에 들어온 영문 type을 내부 categoryId로 변환 (대소문자 무시)
    public static Long fromType(String type) {
        Map<String, Long> mapping = Map.of(
                "politics", 1L,
                "economy", 2L,
                "international", 3L,
                "culture", 4L,
                "society", 5L,
                "it", 6L
        );
        return mapping.get(type.toLowerCase());
    }
}
