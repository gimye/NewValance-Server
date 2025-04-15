package capston.new_valance.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class OnboardingRequest {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;

    @Size(max = 5, message = "최대 5개의 태그만 입력할 수 있습니다.")
    private List<@NotBlank(message = "태그는 빈 문자열일 수 없습니다.") String> tags;
}
