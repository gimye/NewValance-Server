package capston.new_valance.model;

import lombok.Getter;

import java.util.Map;

@Getter
public enum NewsCategory {
    POLITICS(1L, "정치", "politics"),
    ECONOMY(2L, "경제", "economy"),
    INTERNATIONAL(3L, "세계", "international"),
    CULTURE(4L, "생활/문화", "culture"),
    SOCIETY(5L, "IT/과학", "it"),
    IT_SCIENCE(6L, "사회", "society");

    @Getter
    private final Long id;
    private final String koreanName;
    private final String typeName;

    NewsCategory(Long id, String koreanName, String typeName) {
        this.id = id;
        this.koreanName = koreanName;
        this.typeName = typeName;
    }

    public static Long fromType(String type) {
        Map<String, Long> mapping = Map.of(
                "politics", 1L,
                "economy", 2L,
                "international", 3L,
                "culture", 4L,
                "it", 5L,
                "society", 6L
        );
        return mapping.get(type.toLowerCase());
    }
}
