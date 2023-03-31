package liar.memberservice.member.service.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.member.domain.member.Authorities;
import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.member.domain.token.AccessToken;
import liar.memberservice.member.domain.token.RefreshToken;
import liar.memberservice.member.domain.token.Token;
import liar.memberservice.member.repository.rdbms.AuthorityRepository;
import liar.memberservice.member.repository.rdbms.MemberRepository;
import liar.memberservice.member.repository.redis.TokenRepository;
import liar.memberservice.member.service.dto.AuthTokenDto;
import liar.memberservice.member.service.tokenprovider.TokenProviderPolicy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TokenPolicyImplTest {

    @Autowired TokenPolicyImpl tokenPolicyImpl;
    @Autowired RedisTemplate<String, Object> redisTemplate;
    @Autowired MemberRepository memberRepository;
    @Autowired AuthorityRepository authorityRepository;
    @Autowired TokenRepository tokenRepository;
    @Autowired TokenProviderPolicy tokenProviderPolicy;

    static int count = 5;
    static Member[] members = new Member[count];
    static ArrayList<List<Authority>> authorities = new ArrayList<>();

    @AfterEach
    public void tearDown() {
        redisTemplate.delete(redisTemplate.keys("*"));
        authorityRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("1번: 토큰이 모두 없다면 생성하여 저장한다.")
    public void createAuthToken_st() throws Exception {
        //given
        AuthTokenDto[] results = new AuthTokenDto[count];
        createMemberAndAuthorities();

        //when
        createAuthTokenDtos(results);

        //then
        assertionsCreateAuthToken_And_notSameAllTokens(results);

    }

    /**
     * 1번: Assertions
     */
    private void assertionsCreateAuthToken_And_notSameAllTokens(AuthTokenDto[] results) {
        Set<String> accessTokenSet = new HashSet<>();
        Set<String> refreshTokenSet = new HashSet<>();

        for (int i = 0; i < count; i++) {
            assertThat(results[i]).isNotNull();
            accessTokenSet.add(results[i].getAccessToken());
            refreshTokenSet.add(results[i].getRefreshToken());
        }
        assertThat(accessTokenSet.size()).isEqualTo(count);
        assertThat(refreshTokenSet.size()).isEqualTo(count);

    }

    @Test
    @DisplayName("2번: 같은 아이디로 생성요청이 오면, 1회 토큰을 발급하고 나머지 토큰은 동일해야한다. mt")
    public void createAuthToken_sameUserId() throws Exception {
        //given
        AuthTokenDto[] results = new AuthTokenDto[count];
        createMemberAndAuthorities();

        //when
        for (int i = 0; i < count; i++) {
            results[i] = tokenPolicyImpl
                    .createAuthToken(members[0].getUserId(), authorities.get(0));
        }

        //then
        assertionsCreateTokenOnlyFirstRequestAtSameUserId_and_sameTokensAtSameUserId(results);

    }

    /**
     * 2번: Assertions
     */
    private void assertionsCreateTokenOnlyFirstRequestAtSameUserId_and_sameTokensAtSameUserId(AuthTokenDto[] results) {
        Set<String> accessTokenSet = new HashSet<>();
        Set<String> refreshTokenSet = new HashSet<>();

        for (int i = 0; i < count; i++) {
            assertThat(results[i]).isNotNull();
            accessTokenSet.add(results[i].getAccessToken());
            refreshTokenSet.add(results[i].getRefreshToken());
        }
        assertThat(accessTokenSet.size()).isEqualTo(1);
        assertThat(refreshTokenSet.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("3번: 만약 accessToken만 존재한다면, 토큰을 제거하고 accessToken과 refreshToken을 새로 발급 받아야 한다.")
    public void createAuthToken_ifAccessTokenOnlyExists() throws Exception {
        //given
        createMemberAndAuthorities();

        //when
        AccessToken createdToken = (AccessToken) createOnlyOneToken(AccessToken.class);
        tokenRepository.saveToken(createdToken.getId(), createdToken);
        tokenRepository.saveTokenIdx(createdToken.getUserId(), createdToken);

        Token savedBeforeAccessToken = tokenRepository.findTokenByKey(createdToken.getId(), AccessToken.class);

        Thread.sleep(1000L);
        AuthTokenDto authToken = tokenPolicyImpl.createAuthToken(members[0].getUserId(), authorities.get(0));

        Token savedNewAccessToken = tokenRepository.findTokenByIdx(members[0].getUserId(), AccessToken.class);
        Token savedNewRefreshToken = tokenRepository.findTokenByIdx(members[0].getUserId(), RefreshToken.class);

        //then
        /**
         * savedNewTokens는 authToken의 token값이 같아야하며, savedBeforeAccessToken은 값이 달라야한다.
         */
        Assertions.assertThat(authToken.getAccessToken()).isEqualTo(savedNewAccessToken.getId());
        Assertions.assertThat(authToken.getRefreshToken()).isEqualTo(savedNewRefreshToken.getId());
        Assertions.assertThat(authToken.getRefreshToken()).isNotEqualTo(savedBeforeAccessToken.getId());
    }

    @Test
    @DisplayName("4번: 만약 RefreshToken만 존재한다면, 토큰을 제거하고 accessToken과 refreshToken을 새로 발급 받아야 한다.")
    public void createAuthToken_ifRefreshTokenOnlyExists() throws Exception {
        //given
        createMemberAndAuthorities();

        //when
        RefreshToken createdToken = (RefreshToken) createOnlyOneToken(RefreshToken.class);
        tokenRepository.saveToken(createdToken.getId(), createdToken);
        tokenRepository.saveTokenIdx(createdToken.getUserId(), createdToken);

        Token savedBeforeAccessToken = tokenRepository.findTokenByKey(createdToken.getId(), RefreshToken.class);

        Thread.sleep(1000L);
        AuthTokenDto authToken = tokenPolicyImpl.createAuthToken(members[0].getUserId(), authorities.get(0));

        Token savedNewAccessToken = tokenRepository.findTokenByIdx(members[0].getUserId(), AccessToken.class);
        Token savedNewRefreshToken = tokenRepository.findTokenByIdx(members[0].getUserId(), RefreshToken.class);

        //then
        /**
         * savedNewTokens는 authToken의 token값이 같아야하며, savedBeforeAccessToken은 값이 달라야한다.
         */
        Assertions.assertThat(authToken.getAccessToken()).isEqualTo(savedNewAccessToken.getId());
        Assertions.assertThat(authToken.getRefreshToken()).isEqualTo(savedNewRefreshToken.getId());
        Assertions.assertThat(authToken.getRefreshToken()).isNotEqualTo(savedBeforeAccessToken.getId());
    }

    @Test
    @DisplayName("5번: delete Auth Token")
    public void deleteAuthToken() throws Exception {
        //given
        createMemberAndAuthorities();
        AuthTokenDto[] authTokens = new AuthTokenDto[count];
        createAuthTokenDtos(authTokens);

        //when
        for (int i = 0; i < count; i++)
            tokenPolicyImpl.deleteAuthTokens(authTokens[i].getAccessToken(),
                    authTokens[i].getRefreshToken(), authTokens[i].getUserId());

        //then
        assertionsDeleteAllAuthTokens(authTokens);
    }

    /**
     * 5번
     */
    private void assertionsDeleteAllAuthTokens(AuthTokenDto[] authTokens) throws JsonProcessingException {
        for (int i = 0; i < count; i++) {
            assertThat(tokenRepository
                    .findTokenByKey(authTokens[i].getAccessToken(), AccessToken.class)).isNull();
            assertThat(tokenRepository
                    .findTokenByKey(authTokens[i].getRefreshToken(), RefreshToken.class)).isNull();
            assertThat(tokenRepository
                    .findTokenByIdx(authTokens[i].getUserId(), AccessToken.class)).isNull();
            assertThat(tokenRepository
                    .findTokenByIdx(authTokens[i].getUserId(), RefreshToken.class)).isNull();
        }
    }

    @Test
    @DisplayName("6번: reissue는 refreshToken이 아직 유효한 경우 accessToken만 발급한다.")
    public void reissue_refreshTokenTimeValid() throws Exception {
        //given
        createMemberAndAuthorities();
        AuthTokenDto[] authTokens = new AuthTokenDto[count];
        createAuthTokenDtos(authTokens);

        //when
        AuthTokenDto authToken = tokenPolicyImpl.reissueAuthToken(authTokens[0].getRefreshToken());

        //then
        assertionsAccessTokenNotEqualBeforeAndAfter_And_RefreshTokenEqual(authTokens, authToken);
    }

    /**
     * 6번: Assertions
     */
    private static void assertionsAccessTokenNotEqualBeforeAndAfter_And_RefreshTokenEqual(AuthTokenDto[] authTokens,
                                                                                          AuthTokenDto authToken) {
        assertThat(authToken.getRefreshToken()).isEqualTo(authTokens[0].getRefreshToken());
        assertThat(authToken.getAccessToken()).isNotEqualTo(authTokens[0].getAccessToken());
        assertThat(authToken.getUserId()).isEqualTo(authTokens[0].getUserId());
        assertThat(authToken.getAccessToken()).isNotNull();
    }

    /**
     * Test Utils
     */

    private void createMemberAndAuthorities() {
        for (int i = 0; i < count; i++) {
            members[i] = memberRepository.save(Member.builder()
                            .email("kose" + i + "@naver.com")
                            .userId(UUID.randomUUID().toString())
                            .password(UUID.randomUUID().toString())
                    .build());
            authorities
                    .add(Arrays.asList(authorityRepository.save(new Authority(members[i], Authorities.ROLE_USER))));
        }
    }

    private Token createOnlyOneToken(Class<?> clazz) {
        Token token;
        if (clazz.equals(AccessToken.class)) {
            String accessToken = tokenProviderPolicy.createAccessToken(members[0].getUserId(), authorities.get(0));
            token = AccessToken.of(accessToken, members[0].getUserId(), 10000L);
        }
        else {
            String refreshToken = tokenProviderPolicy.createRefreshToken(members[0].getUserId(), authorities.get(0));
            token = RefreshToken.of(refreshToken, members[0].getUserId(), 10000L);
        }
        return token;
    }

    private void createAuthTokenDtos(AuthTokenDto[] authTokenDtos) throws JsonProcessingException {
        for (int i = 0; i < count; i++)
            authTokenDtos[i] = tokenPolicyImpl
                    .createAuthToken(members[i].getUserId(), authorities.get(i));
    }


}