package liar.memberservice.member.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FormRegisterUserDto {

    private String username;
    private String email;
    private String password;

    @Builder
    public FormRegisterUserDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
