package liar.memberservice.member.service.tokenprovider;

import io.jsonwebtoken.Claims;
import liar.memberservice.member.domain.member.Authority;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TokenProviderPolicy {

    String TOKEN_TYPE = "Bearer ";

    String createAccessToken(Authentication authentication);

    String createRefreshToken(Authentication authentication);

    String createAccessToken(String userId, List<Authority> roles);

    String createRefreshToken(String userId, List<Authority> roles);

    Claims getClaims(String token);

    String getUserIdFromToken(String token);

    long getRemainingTimeFromToken(String token);

    boolean isMoreThanReissueTime(String token);

    Authentication getAuthentication(String token);

    boolean validateToken(String authToken);

    String removeType(String token);

    Long getRemainTime(String token);

    String createToken(Authentication authentication, long tokenTime);

    String createToken(String userId, List<Authority> authorities, long tokenTime);


}