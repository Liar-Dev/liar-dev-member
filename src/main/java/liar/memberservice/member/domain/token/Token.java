package liar.memberservice.member.domain.token;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Token {

    @Id
    private String id;

    @Indexed
    private String userId;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private long expiration;
}
