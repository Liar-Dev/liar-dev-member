package liar.memberservice.member.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.member.domain.session.LoginSession;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginSessionRepository {

    void saveLoginSession(LoginSession loginSession) throws JsonProcessingException;
    boolean existLoginSession(String userId);
    void deleteLoginSession(String userId);
    LoginSession findLoginSessionByUserId(String userId) throws JsonProcessingException;

}
