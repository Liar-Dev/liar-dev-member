package liar.memberservice.member.service.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.exception.exception.DoubleLoginException;
import liar.memberservice.exception.exception.NotExistRefreshTokenException;
import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.token.*;
import liar.memberservice.member.repository.redis.TokenRepositoryImpl;
import liar.memberservice.member.service.member.MemberPolicy;
import liar.memberservice.member.service.dto.AuthTokenDto;
import liar.memberservice.member.service.tokenprovider.TokenProviderPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenPolicyImpl implements TokenPolicy {

    private final TokenRepositoryImpl tokenRepository;
    private final TokenProviderPolicy tokenProviderPolicy;
    private final MemberPolicy memberPolicy;

    @Override
    public AuthTokenDto createAuthToken(String userId, List<Authority> authorities) throws JsonProcessingException {
        AuthTokenDto authTokenDto = findOrDeleteToken(userId);
        if (authTokenDto != null) {
            return authTokenDto;
        }
        return saveAndGetToken(userId, authorities);
    }


    @Override
    public AuthTokenDto createAuthToken(Authentication authentication) throws JsonProcessingException {
        String accessToken = tokenProviderPolicy.createAccessToken(authentication);
        String newRefreshToken = createNewRefreshToken(authentication);
        return new AuthTokenDto(accessToken, newRefreshToken, getUserId(authentication));
    }

    @Override
    public void deleteAuthTokens(String accessToken, String refreshToken, String userId) throws JsonProcessingException {
        deleteTokenByType(accessToken, userId, AccessToken.class);
        deleteTokenByType(refreshToken, userId, RefreshToken.class);
    }

    @Override
    public AuthTokenDto reissueAuthToken(String refreshToken) throws JsonProcessingException {
        String userId = getUserIdOrThrow(refreshToken);
        Authentication authentication = tokenProviderPolicy.getAuthentication(refreshToken);
        if (tokenProviderPolicy.isMoreThanReissueTime(refreshToken))
            return AuthTokenDto.of(tokenProviderPolicy.createAccessToken(authentication),
                    refreshToken, getUserId(authentication));

        deleteTokenByType(refreshToken, userId, RefreshToken.class);
        return AuthTokenDto.of(
                tokenProviderPolicy.createAccessToken(authentication),
                createNewRefreshToken(authentication),
                getUserId(authentication)
        );
    }

    @Override
    public void saveLogoutTokensAndDeleteSavedTokens(String userId) throws JsonProcessingException {
        Token savedAccessToken = tokenRepository.findTokenByIdx(userId, AccessToken.class);
        Token savedRefreshToken = tokenRepository.findTokenByIdx(userId, RefreshToken.class);

        saveLogoutTokens(savedAccessToken, savedRefreshToken);
        deleteAuthTokens(savedAccessToken.getId(), savedRefreshToken.getId(), userId);
    }

    public void throwIfLogoutSessionTokens(String accessToken, String refreshToken, String userId)
            throws JsonProcessingException {

        Token logoutAccessToken = tokenRepository.findTokenByKey(accessToken, LogoutSessionAccessToken.class);
        Token logoutRefreshToken = tokenRepository.findTokenByKey(refreshToken, LogoutSessionRefreshToken.class);

        if ((logoutAccessToken != null && logoutAccessToken.getUserId().equals(userId)) ||
                (logoutRefreshToken != null && logoutRefreshToken.getUserId().equals(userId)))
            throw new DoubleLoginException();
    }

    private void saveLogoutTokens(Token savedAccessToken, Token savedRefreshToken) throws JsonProcessingException {
        tokenRepository.saveToken(savedAccessToken.getId(), mapperAccess(savedAccessToken));
        tokenRepository.saveToken(savedRefreshToken.getId(), mapperRefresh(savedRefreshToken));
    }

    private LogoutSessionAccessToken mapperAccess(Token accessToken) {
        return LogoutSessionAccessToken
                .of(accessToken.getId(), accessToken.getUserId(), accessToken.getExpiration());
    }

    private LogoutSessionRefreshToken mapperRefresh(Token refreshToken) {
        return LogoutSessionRefreshToken
                .of(refreshToken.getId(), refreshToken.getUserId(), refreshToken.getExpiration());
    }


    private AuthTokenDto findOrDeleteToken(String userId) throws JsonProcessingException {
        Token savedAccessToken = tokenRepository.findTokenByIdx(userId, AccessToken.class);
        Token savedRefreshToken = tokenRepository.findTokenByIdx(userId, RefreshToken.class);

        if (savedAccessToken != null && savedRefreshToken != null) {
            return new AuthTokenDto(savedAccessToken.getId(), savedRefreshToken.getId(), userId);
        }
        else if (savedAccessToken != null) {
            deleteTokenByType(savedAccessToken.getId(), savedAccessToken.getUserId(), AccessToken.class);
        }

        else if (savedRefreshToken != null) {
            deleteTokenByType(savedRefreshToken.getId(), savedRefreshToken.getUserId(), RefreshToken.class);
        }

        return null;
    }

    private AuthTokenDto saveAndGetToken(String userId, List<Authority> authorities) throws JsonProcessingException {
        String accessToken = tokenProviderPolicy.createAccessToken(userId, authorities);
        String refreshToken = tokenProviderPolicy.createRefreshToken(userId, authorities);

        saveTokens(AccessToken.of(accessToken, userId, tokenProviderPolicy.getRemainingTimeFromToken(accessToken)),
                RefreshToken.of(refreshToken, userId, tokenProviderPolicy.getRemainingTimeFromToken(refreshToken)));

        return new AuthTokenDto(accessToken, refreshToken, userId);
    }

    private void saveTokens(AccessToken accessToken, RefreshToken refreshToken) throws JsonProcessingException {
        tokenRepository.saveToken(accessToken.getId(), accessToken);
        tokenRepository.saveTokenIdx(accessToken.getUserId(), accessToken);
        tokenRepository.saveToken(refreshToken.getId(), refreshToken);
        tokenRepository.saveTokenIdx(refreshToken.getUserId(), refreshToken);
    }

    private String getUserIdOrThrow(String refreshToken) throws JsonProcessingException {
        Token token = tokenRepository.findTokenByKey(refreshToken, RefreshToken.class);
        if (token == null) {
            throw new NotExistRefreshTokenException();
        }
        return token.getUserId();
    }

    private String getUserId(Authentication authentication) {
        return memberPolicy.findByUserId(authentication.getName()).getUserId();
    }

    private String createNewRefreshToken(Authentication authentication) throws JsonProcessingException {
        String newRefreshToken = tokenProviderPolicy.createRefreshToken(authentication);

        RefreshToken refreshToken = RefreshToken.of(newRefreshToken, authentication.getName(),
                tokenProviderPolicy.getRemainingTimeFromToken(newRefreshToken));

        tokenRepository.saveToken(refreshToken.getId(), refreshToken);

        return refreshToken.getId();
    }

    private void deleteTokenByType(String tokenId, String userId, Class<?> clazz) throws JsonProcessingException {
        tokenRepository.deleteToken(tokenId, clazz);
        tokenRepository.deleteTokenIdx(userId, clazz);
    }
}
