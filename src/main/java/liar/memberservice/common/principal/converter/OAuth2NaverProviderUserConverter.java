package liar.memberservice.common.principal.converter;


import liar.memberservice.common.principal.enums.SocialType;
import liar.memberservice.common.principal.util.OAuth2Utils;
import liar.memberservice.common.principal.social.NaverUser;
import liar.memberservice.common.principal.social.ProviderUser;

public final class OAuth2NaverProviderUserConverter implements ProviderUserConverter<ProviderUserRequest, ProviderUser> {
    @Override
    public ProviderUser converter(ProviderUserRequest providerUserRequest) {

        if (!providerUserRequest.getClientRegistration().getRegistrationId().equals(
                SocialType.NAVER.getSocialName()
        )) return null;

        return new NaverUser(
                OAuth2Utils.getSubAttributes(providerUserRequest.getOAuth2User(), "response"),
                providerUserRequest.getOAuth2User(),
                providerUserRequest.getClientRegistration()
        );

    }
}
