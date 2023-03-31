package liar.memberservice.member.domain.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class LogoutSessionAccessToken extends Token implements Serializable {

    private LogoutSessionAccessToken(String id, String userId, long expiration) {
        super(id, userId, expiration);
    }
    public static LogoutSessionAccessToken of (String logoutSessionAccessToken, String userId, Long expiration) {
        return new LogoutSessionAccessToken(logoutSessionAccessToken, userId, expiration);
    }
}
