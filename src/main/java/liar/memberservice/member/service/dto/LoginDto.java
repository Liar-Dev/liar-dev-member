package liar.memberservice.member.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    @NotNull
    private String email;

    @NotNull
    private String password;

}
