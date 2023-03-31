package liar.memberservice.member.service.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.service.dto.AuthTokenDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TokenPolicy {

    AuthTokenDto createAuthToken(String userId, List<Authority> authorities) throws JsonProcessingException;

    AuthTokenDto createAuthToken(Authentication authentication) throws JsonProcessingException;

    AuthTokenDto reissueAuthToken(String refreshToken) throws JsonProcessingException;

    void deleteAuthTokens(String accessToken, String refreshToken, String userId) throws JsonProcessingException;

    void saveLogoutTokensAndDeleteSavedTokens(String userId) throws JsonProcessingException;

}
