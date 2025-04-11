package capston.new_valance.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class OnboardingRequest {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;
}
