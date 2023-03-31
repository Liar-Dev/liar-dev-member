package liar.memberservice.common.principal.converter;

import liar.memberservice.common.principal.enums.SocialType;
import liar.memberservice.common.principal.util.OAuth2Utils;
import liar.memberservice.common.principal.social.GoogleUser;
import liar.memberservice.common.principal.social.ProviderUser;

public final class OAuth2GoogleProviderUserConverter implements ProviderUserConverter<ProviderUserRequest, ProviderUser> {
    @Override
    public ProviderUser converter(ProviderUserRequest providerUserRequest) {

        if (!providerUserRequest.getClientRegistration().getRegistrationId().equals(
                SocialType.GOOGLE.getSocialName()
        )) return null;

        return new GoogleUser(
                OAuth2Utils.getMainAttributes(providerUserRequest.getOAuth2User()),
                providerUserRequest.getOAuth2User(),
                providerUserRequest.getClientRegistration()
        );
    }
}
