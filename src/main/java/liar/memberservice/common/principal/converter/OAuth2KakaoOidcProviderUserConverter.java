package liar.memberservice.common.principal.converter;

import liar.memberservice.common.principal.enums.SocialType;
import liar.memberservice.common.principal.util.OAuth2Utils;
import liar.memberservice.common.principal.social.KakaoOidcUser;
import liar.memberservice.common.principal.social.ProviderUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public final class OAuth2KakaoOidcProviderUserConverter implements ProviderUserConverter<ProviderUserRequest, ProviderUser> {

    @Override
    public ProviderUser converter(ProviderUserRequest providerUserRequest) {
        if (!providerUserRequest.getClientRegistration().getRegistrationId().equals(SocialType.KAKAO.getSocialName())) {
            return null;
        }

        if (!(providerUserRequest.getOAuth2User() instanceof OidcUser)) {
            return null;
        }

        return new KakaoOidcUser(OAuth2Utils.getMainAttributes(
                providerUserRequest.getOAuth2User()),
                providerUserRequest.getOAuth2User(),
                providerUserRequest.getClientRegistration());
    }
}
