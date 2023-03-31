package liar.memberservice.member.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import liar.memberservice.member.domain.token.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class TokenRepositoryImpl<T extends Token> implements TokenRepository<Token> {
    private final RedisTemplate<String, Object> redisTemplate;

    @Qualifier("redisObjectMapper")
    private final ObjectMapper objectMapper;

    public TokenRepositoryImpl(RedisTemplate<String, Object> redisTemplate,
                               @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private final static String IDX = "idx";

    @Override
    public void saveToken(String key, Token token) throws JsonProcessingException {
        key = getKey(token.getClass().getSimpleName(), key);
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(token));
        redisTemplate.expire(key, token.getExpiration(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Token findTokenByKey(String key, Class<Token> clazz) throws JsonProcessingException {
        key = getKey(clazz.getSimpleName(), key);
        return getObjectValue(key, clazz);
    }

    @Override
    public void saveTokenIdx(String key, Token token) throws JsonProcessingException {
        key = getKey(token.getClass().getSimpleName(), key, IDX);
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(token.getId()));
        redisTemplate.expire(key, token.getExpiration(), TimeUnit.MILLISECONDS);
    }

    @Override
    public String findTokenIdxValue(String key, Class<Token> clazz) throws JsonProcessingException {
        key = getKey(clazz.getSimpleName(), key, IDX);
        return getIdxValue(key);
    }

    @Override
    public Token findTokenByIdx(String key, Class<Token> clazz) throws JsonProcessingException {

        key = findTokenIdxValue(key, clazz);
        if (key == null) return null;

        return findTokenByKey(key, clazz);
    }

    @Override
    public void deleteToken(String key, Class<Token> clazz) throws JsonProcessingException {
        key = getKey(clazz.getSimpleName(), key);
        redisTemplate.delete(key);
    }

    @Override
    public void deleteTokenIdx(String key, Class<Token> clazz) throws JsonProcessingException {
        key = getKey(clazz.getSimpleName(), key, IDX);
        redisTemplate.delete(key);
    }

    private Token getObjectValue(String key, Class<Token> clazz) throws JsonProcessingException {
        String value = (String) redisTemplate.opsForValue().get(key);

        if (value == null || value.isEmpty()) return null;
        return objectMapper.readValue(value, clazz);
    }

    private String getKey(String... keys) {
        return String.join(":", keys);
    }

    private String getIdxValue(String key) throws JsonProcessingException {
        String value = (String) redisTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(value)) return null;
        return objectMapper.readValue(value, String.class);
    }
}