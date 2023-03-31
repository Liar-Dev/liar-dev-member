package liar.memberservice.member.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class FormRegisterRequest {

    @NotNull
    private String username;

    @NotNull
    private String email;

    @NotNull
    @Length(min = 10, max = 100, message = "password는 10자 이상입니다.")
    private String password;

    @Builder
    public FormRegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

}
