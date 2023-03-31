package liar.memberservice.member.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.member.domain.token.Token;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository<T extends Token> {

    void saveToken(String key, T t) throws JsonProcessingException;

    T findTokenByKey(String key, Class<T> clazz) throws JsonProcessingException;

    void saveTokenIdx(String key, T t) throws JsonProcessingException;

    String findTokenIdxValue(String key, Class<T> clazz) throws JsonProcessingException;

    T findTokenByIdx(String key, Class<T> clazz) throws JsonProcessingException;

    void deleteToken(String key, Class<T> clazz) throws JsonProcessingException;

    void deleteTokenIdx(String key, Class<T> clazz) throws JsonProcessingException;

}