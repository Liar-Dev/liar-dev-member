package liar.memberservice.common.principal.service;

import liar.memberservice.common.principal.converter.ProviderUserConverter;
import liar.memberservice.common.principal.converter.ProviderUserRequest;
import liar.memberservice.common.principal.PrincipalUser;
import liar.memberservice.common.principal.social.ProviderUser;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.member.service.member.MemberPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService extends AbstractOAuth2UserService implements UserDetailsService {

    public CustomUserDetailsService(MemberPolicy memberPolicy, ProviderUserConverter<ProviderUserRequest,
                ProviderUser> providerUserConverter) {
        super(memberPolicy, providerUserConverter);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = getMemberPolicy().findMemberByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException("존재하지 않는 회원입니다.");
        }


        // converter 처리
        ProviderUserRequest providerUserRequest = new ProviderUserRequest(member);
        ProviderUser providerUser = providerUser(providerUserRequest);

        return new PrincipalUser(providerUser);

    }



}

