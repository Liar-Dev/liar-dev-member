package liar.memberservice.member.repository.redis;

import liar.memberservice.member.domain.session.LoginSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LoginSessionRepositoryTest {

    @Autowired
    LoginSessionRepository loginSessionRepository;

    @Autowired
    RedisTemplate redisTemplate;

    @AfterEach
    public void tearDown() {
        redisTemplate.delete(redisTemplate.keys("*"));
    }

    static String userId = UUID.randomUUID().toString();
    static String remoteAddr = " 127.0.0.1";
    static Long expiration = 10000L;

    @Test
    @DisplayName("saveLoginSession")
    public void saveLoginSession() throws Exception {
        //given
        LoginSession loginSession = LoginSession.of(userId, remoteAddr, expiration);

        //when
        loginSessionRepository.saveLoginSession(loginSession);
        LoginSession findLoginSession = loginSessionRepository.findLoginSessionByUserId(userId);

        //then
        assertThat(findLoginSession.getUserId()).isEqualTo(userId);
        assertThat(findLoginSession.getRemoteAddr()).isEqualTo(remoteAddr);
        assertThat(findLoginSession.getExpiration()).isEqualTo(expiration);
        assertThat(findLoginSession.getCreatedAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("deleteLoginSession")
    public void deleteLoginSession() throws Exception {
        //given
        LoginSession loginSession = LoginSession.of(userId, remoteAddr, expiration);
        loginSessionRepository.saveLoginSession(loginSession);

        //when
        loginSessionRepository.deleteLoginSession(userId);

        //then
        assertThat(loginSessionRepository.findLoginSessionByUserId(userId)).isNull();
    }


}