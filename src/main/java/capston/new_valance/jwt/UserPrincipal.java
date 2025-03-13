package capston.new_valance.jwt;

import capston.new_valance.model.LoginProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class UserPrincipal implements Serializable {
    private Long userId;
    private String username;
    private String email;
    private LoginProvider provider;
}