package liar.memberservice.member.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import liar.memberservice.member.domain.session.LoginSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class LoginSessionRepositoryImpl implements LoginSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public LoginSessionRepositoryImpl(RedisTemplate<String, Object> redisTemplate,
                                      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private final static String LOGIN_SESSION = "LoginSession:";

    @Override
    public void saveLoginSession(LoginSession loginSession) throws JsonProcessingException {
        String key = getLoginSessionKey(loginSession.getUserId());

        redisTemplate.opsForValue().set(key,
                objectMapper.writeValueAsString(loginSession));
        redisTemplate.expire(key, loginSession.getExpiration(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean existLoginSession(String userId) {
        return redisTemplate.hasKey(getLoginSessionKey(userId));
    }

    @Override
    public void deleteLoginSession(String userId) {
        if (existLoginSession(userId)) redisTemplate.delete(getLoginSessionKey(userId));
    }

    @Override
    public LoginSession findLoginSessionByUserId(String userId) throws JsonProcessingException {
        if (existLoginSession(userId)) {
            String value = (String) redisTemplate.opsForValue().get(getLoginSessionKey(userId));

            if (value == null) return null;
            return objectMapper.readValue(value, LoginSession.class);
        }
        return null;
    }
    private String getLoginSessionKey(String userId) {
        return LOGIN_SESSION + userId;
    }
}
