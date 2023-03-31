package liar.memberservice.member.service.loginsession;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.common.aop.anno.RedisTransactionalAuthTokenDto;
import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.member.domain.session.LoginSession;
import liar.memberservice.member.repository.redis.LoginSessionRepository;
import liar.memberservice.member.service.dto.AuthTokenDto;
import liar.memberservice.member.service.token.TokenPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSessionPolicyImpl implements LoginSessionPolicy {

    private final LoginSessionRepository loginSessionRepository;
    private final TokenPolicy tokenPolicy;

    @Value("${jwt.access-expiration-time}")
    private Long expiration;

    @RedisTransactionalAuthTokenDto
    @Override
    public AuthTokenDto loginNewSession(Member member, List<Authority> authorities, String remoteAddr)
            throws JsonProcessingException {

        if (isDoubleLogin(member.getUserId())) {
            logoutSession(member.getUserId());
            tokenPolicy.saveLogoutTokensAndDeleteSavedTokens(member.getUserId());
        }
        loginSession(member.getUserId(), remoteAddr);

        return tokenPolicy.createAuthToken(member.getUserId(), authorities);
    }

    @Override
    public LoginSession findLoginSessionByUserId(String userId) throws JsonProcessingException {
        return loginSessionRepository.findLoginSessionByUserId(userId);
    }

    private boolean isDoubleLogin(String userId) {
        return loginSessionRepository.existLoginSession(userId);
    }

    private void loginSession(String userId, String remoteAddr) throws JsonProcessingException {
        loginSessionRepository.saveLoginSession(LoginSession.of(userId, remoteAddr, expiration));
    }

    private void logoutSession(String userId) {
        loginSessionRepository.deleteLoginSession(userId);
    }
}
