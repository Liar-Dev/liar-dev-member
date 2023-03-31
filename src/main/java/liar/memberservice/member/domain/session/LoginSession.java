package liar.memberservice.member.domain.session;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static liar.memberservice.member.domain.session.LoginStatus.ON;

@Getter
@NoArgsConstructor
public class LoginSession {

    private String userId;
    private String remoteAddr;
    private LoginStatus loginStatus = ON;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long expiration;

    public LoginSession(String userId, String remoteAddr, Long expiration) {
        this.userId = userId;
        this.remoteAddr = remoteAddr;
        this.createdAt = LocalDateTime.now();
        this.expiration = expiration;
    }

    public static LoginSession of(String userId, String remoteAddr, Long expiration) {
        return new LoginSession(userId, remoteAddr, expiration);
    }
}

