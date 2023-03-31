package liar.memberservice.member.service.loginsession;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.member.domain.session.LoginSession;
import liar.memberservice.member.service.dto.AuthTokenDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LoginSessionPolicy {

    AuthTokenDto loginNewSession(Member member, List<Authority> authorities, String remoteAddr)
            throws JsonProcessingException;

    LoginSession findLoginSessionByUserId(String userId) throws JsonProcessingException;

}
