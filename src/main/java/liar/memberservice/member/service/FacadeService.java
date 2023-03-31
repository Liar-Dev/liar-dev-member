package liar.memberservice.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.member.controller.dto.request.LoginFacadeRequest;
import liar.memberservice.member.service.dto.AuthTokenDto;
import liar.memberservice.member.service.dto.MemberInfoDto;
import liar.memberservice.member.controller.dto.request.FormRegisterRequest;
import liar.memberservice.member.controller.util.RequestMapperFactory;
import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.exception.exception.UserRegisterConflictException;
import liar.memberservice.member.service.loginsession.LoginSessionPolicy;
import liar.memberservice.member.service.member.MemberPolicy;
import liar.memberservice.member.service.token.TokenPolicyImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacadeService {

    private final MemberPolicy memberPolicy;
    private final TokenPolicyImpl tokenPolicyImpl;

    private final LoginSessionPolicy loginSessionPolicy;


    /**
     * register
     */
    public void register(FormRegisterRequest request) {
        if (!memberPolicy.register(RequestMapperFactory.mapper(request))) {
            throw new UserRegisterConflictException();
        }
    }

    /**
     * login
     */
    public AuthTokenDto login(LoginFacadeRequest request) throws JsonProcessingException {
        Member member = memberPolicy.findMemberByEmailOrThrow(RequestMapperFactory.mapper(request));
        List<Authority> authorities = memberPolicy.findAuthorityByUserOrThrow(member);

        return loginSessionPolicy.loginNewSession(member, authorities, request.getRemoteAddr());
    }

    /**
     * login
     */
    public AuthTokenDto login(Authentication authentication) throws JsonProcessingException {
        return tokenPolicyImpl.createAuthToken(authentication);
    }

    /**
     * logout
     */
    public void logout(String accessToken, String refreshToken, String userId) throws JsonProcessingException {
        tokenPolicyImpl.deleteAuthTokens(accessToken, refreshToken, userId);
    }

    /**
     * reissue
     */
    public AuthTokenDto reissue(String refreshToken) throws JsonProcessingException {
        return tokenPolicyImpl.reissueAuthToken(refreshToken);
    }

    /**
     * getMemberInfo
     */
    @Transactional(readOnly = true)
    public MemberInfoDto getMemberInfo(String userId) {
        return new MemberInfoDto(memberPolicy.findByUserId(userId));
    }
}
