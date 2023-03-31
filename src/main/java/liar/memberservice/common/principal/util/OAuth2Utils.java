package liar.memberservice.common.principal.util;

import liar.memberservice.common.principal.Attributes;
import liar.memberservice.common.principal.PrincipalUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

@Slf4j
public class OAuth2Utils {

    public static Attributes getMainAttributes(OAuth2User oAuth2User) {

        return Attributes.builder()
                .mainAttributes(oAuth2User.getAttributes())
                .build();
    }

    public static Attributes getSubAttributes(OAuth2User oAuth2User, String mainAttributesKey) {

        Map<String, Object> subAttributes = (Map<String, Object>) oAuth2User.getAttributes().get(mainAttributesKey);

        return Attributes
                .builder()
                .subAttributes(subAttributes)
                .build();
    }

    public static Attributes getOtherAttributes(OAuth2User oAuth2User,
                                                String mainAttributesKey, String subAttributesKey) {
        Map<String, Object> subAttributes = (Map<String, Object>) oAuth2User.getAttributes().get(mainAttributesKey);
        Map<String, Object> otherAttributes = (Map<String, Object>) subAttributes.get(subAttributesKey);

        return Attributes.builder()
                .subAttributes(subAttributes)
                .otherAttributes(otherAttributes)
                .build();
    }

    public static String oAuth2UserName(OAuth2AuthenticationToken auth2AuthenticationToken,
                                        PrincipalUser principalUser) {

        Attributes attributes;
        String registrationId = auth2AuthenticationToken.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = principalUser.getProviderUser().getOAuth2User();

        switch (registrationId.toLowerCase()) {

            case "google":
                attributes = OAuth2Utils.getMainAttributes(oAuth2User);
                return (String) attributes.getMainAttributes().get("name");

            case "naver":
                attributes = OAuth2Utils.getSubAttributes(oAuth2User, "response");
                return (String) attributes.getSubAttributes().get("name");

            case "kakao":
                if (oAuth2User instanceof OidcUser) {
                    attributes = OAuth2Utils.getMainAttributes(oAuth2User);
                    return (String) attributes.getMainAttributes().get("nickname");
                } else {
                    attributes = OAuth2Utils.getOtherAttributes(principalUser, "kakao_account", "profile");
                    return (String) attributes.getOtherAttributes().get("nickname");
                }

            default:
                return null;
        }
    }
}
