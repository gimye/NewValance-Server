package capston.new_valance.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyWatchCountResponse {
    private final String date;
    private final int value;
}
