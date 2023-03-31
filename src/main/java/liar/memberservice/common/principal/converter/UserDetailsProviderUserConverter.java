package liar.memberservice.common.principal.converter;

import liar.memberservice.common.principal.form.FormUser;
import liar.memberservice.common.principal.social.ProviderUser;
import liar.memberservice.member.domain.member.Member;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class UserDetailsProviderUserConverter implements ProviderUserConverter<ProviderUserRequest, ProviderUser> {

    @Override
    public ProviderUser converter(ProviderUserRequest providerUserRequest) {

        Member member = providerUserRequest.getMember();
        if (member == null) {
            return null;
        }

        return FormUser.builder()
                .id(member.getEmail())
                .username(member.getUsername())
                .password(member.getPassword())
                .email(member.getEmail())
                .provider("none")
                .build();
    }
}
