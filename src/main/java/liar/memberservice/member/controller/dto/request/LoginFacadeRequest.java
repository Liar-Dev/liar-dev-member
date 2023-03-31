package liar.memberservice.member.controller.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginFacadeRequest {

    private String email;
    private String password;
    private String remoteAddr;

    @Builder
    public LoginFacadeRequest(String email, String password, String remoteAddr) {
        this.email = email;
        this.password = password;
        this.remoteAddr = remoteAddr;
    }
}
