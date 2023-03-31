package liar.memberservice.member.domain.token;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class LogoutSessionRefreshToken extends Token implements Serializable {

    private LogoutSessionRefreshToken(String id, String userId, long expiration) {
        super(id, userId, expiration);
    }
    public static LogoutSessionRefreshToken of (String logoutSessionRefreshToken, String userId, Long expiration) {
        return new LogoutSessionRefreshToken(logoutSessionRefreshToken, userId, expiration);
    }

}
