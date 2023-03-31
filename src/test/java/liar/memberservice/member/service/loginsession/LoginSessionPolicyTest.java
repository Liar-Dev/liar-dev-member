package liar.memberservice.member.service.loginsession;

import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.member.domain.session.LoginSession;
import liar.memberservice.member.domain.token.*;
import liar.memberservice.member.repository.rdbms.AuthorityRepository;
import liar.memberservice.member.repository.rdbms.MemberRepository;
import liar.memberservice.member.repository.redis.TokenRepository;
import liar.memberservice.member.service.dto.AuthTokenDto;
import liar.memberservice.member.service.dto.FormRegisterUserDto;
import liar.memberservice.member.service.member.MemberPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Integer.parseInt;
import static java.time.LocalDateTime.now;
import static liar.memberservice.member.domain.session.LoginStatus.ON;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LoginSessionPolicyTest {

    @Autowired TokenRepository tokenRepository;
    @Autowired LoginSessionPolicy loginSessionPolicy;
    @Autowired MemberRepository memberRepository;
    @Autowired AuthorityRepository authorityRepository;
    @Autowired MemberPolicy memberPolicy;
    @Autowired RedisTemplate redisTemplate;

    static int count = 3;
    static String email = "kose@naver.com";
    static String username = "gosekose";
    static String password = UUID.randomUUID().toString();
    static Member member;
    static List<Authority> authorities;

    @BeforeEach
    public void init() {
        memberPolicy.register(FormRegisterUserDto.builder()
                .email(email).username(username).password(password).build());

        member = memberRepository.findByEmail(email);
        authorities = authorityRepository.findAuthorityByMember(member);

        if(member == null || authorities == null) throw new RuntimeException("초기화 예외");
    }


    @AfterEach
    public void tearDown() {
        authorityRepository.deleteAll();
        memberRepository.deleteAll();
//        redisTemplate.delete(redisTemplate.keys("*"));
    }

    @Test
    @DisplayName("이전의 로그인 정보없이 loginNewSession 로그인을 하면 새로운 세션을 저장하고 토큰을 반환한다.")
    public void loginNewSession_success() throws Exception {
        //given
        String remoteAddr = "127.0.0.1";

        //when
        AuthTokenDto authTokenDto = loginSessionPolicy.loginNewSession(member, authorities, remoteAddr);
        LoginSession savedLoginSession = loginSessionPolicy.findLoginSessionByUserId(member.getUserId());

        //then
        assertThat(authTokenDto.getAccessToken()).isNotNull();
        assertThat(authTokenDto.getRefreshToken()).isNotNull();
        assertThat(authTokenDto.getUserId()).isEqualTo(member.getUserId());

        assertThat(savedLoginSession.getLoginStatus()).isEqualTo(ON);
        assertThat(savedLoginSession.getRemoteAddr()).isEqualTo(remoteAddr);
        assertThat(savedLoginSession.getUserId()).isEqualTo(member.getUserId());
    }


    /**
     * 체크 포인트
     * 새로운 새션으로 로그인 정보가 등록되어야 한다.
     * ---> 기존에 있던 로그인세션은 제거되고 새로운 로그인 세션을 등록한다
     * ---> 기존에 있던 토큰은 logoutSessionToken에 저장되어야 한다.
     * ---> 토큰 정보는 새로운 AccessToken, RefreshToken으로 업데이트 되어야 한다.
     */
    @Test
    @DisplayName("이전에 로그인한 사용자 정보를 제거하고 새로운 세션으로 로그인을 진행한다.")
    public void loginNewSession_success_beforeLoginSessionExist() throws Exception {
        //given
        String beforeRemoteAddr = "127.0.0.1";
        AuthTokenDto beforeAuthToken = loginSessionPolicy.loginNewSession(member, authorities, beforeRemoteAddr);

        //when
        Thread.sleep(2000L);
        String newRemoteAddr = "127.1.1.1";
        AuthTokenDto newAuthToken = loginSessionPolicy.loginNewSession(member, authorities, newRemoteAddr);
        LoginSession savedLoginSession = loginSessionPolicy.findLoginSessionByUserId(member.getUserId());

        Thread.sleep(2000L);
        Token logoutAccessToken = tokenRepository
                .findTokenByKey(beforeAuthToken.getAccessToken(), LogoutSessionAccessToken.class);

        Token logoutRefreshToken = tokenRepository
                .findTokenByKey(beforeAuthToken.getRefreshToken(), LogoutSessionRefreshToken.class);


        //then
        assertThat(savedLoginSession.getUserId()).isEqualTo(member.getUserId());
        assertThat(savedLoginSession.getRemoteAddr()).isEqualTo(newRemoteAddr);
        assertThat(savedLoginSession.getLoginStatus()).isEqualTo(ON);
        assertThat(savedLoginSession.getCreatedAt()).isBefore(now());
        assertThat(savedLoginSession.getExpiration()).isNotNull();

        assertThat(beforeAuthToken.getAccessToken()).isNotEqualTo(newAuthToken.getAccessToken());
        assertThat(beforeAuthToken.getRefreshToken()).isNotEqualTo(newAuthToken.getRefreshToken());
        assertThat(beforeAuthToken.getUserId()).isEqualTo(newAuthToken.getUserId());

        assertThat(logoutAccessToken.getId()).isEqualTo(beforeAuthToken.getAccessToken());
        assertThat(logoutRefreshToken.getId()).isEqualTo(beforeAuthToken.getRefreshToken());
    }

    /**
     * 동시에 로그인 요청이 올 때 (인터셉터에서 처리되지 않는 제약 사항을 넘어서 요청이 수행되었다고 가정)
     * 같은 email, password, 다른 remoteAddr
     * 마지막에 실행되는 익명의 스레드의 로그인 세션만 유지하고 다른 스레드의 로그인 세션은 제거한다.
     * 유효 한인증 토큰은 하나만 유지되고 나머지 토큰은 모두 LogoutSessionToken으로 저장된다.
     */
    @Test
    @DisplayName("로그인이 여러 번 발생하는 문제에 대해 동시성을 체크한다.")
    public void loginNewSession_success_beforeLoginSessionExist_mt() throws Exception {
        //given
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(count);
        AuthTokenDto[] authTokens = new AuthTokenDto[count];

        //when
        for (int i = 0; i < count; i++) {
            int fIdx = i;
            executorService.submit(() -> {
                try {
                    authTokens[fIdx] = loginSessionPolicy
                            .loginNewSession(member, authorities, "127.0.0." + fIdx);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Thread.sleep(3000);
        LoginSession nowLoginSession = loginSessionPolicy.findLoginSessionByUserId(member.getUserId());

        //then
        int validRemoteAddr = parseInt(nowLoginSession.getRemoteAddr().split("\\.")[3]);
        System.out.println("validRemoteAddr = " + validRemoteAddr);
        Thread.sleep(3000);

        for (int i = 0; i < count; i++) {
            if (i != validRemoteAddr) {
                assertThat(tokenRepository
                        .findTokenByKey(authTokens[i].getAccessToken(), AccessToken.class)).isNull();
                assertThat(tokenRepository
                        .findTokenByKey(authTokens[i].getRefreshToken(), RefreshToken.class)).isNull();
                assertThat(tokenRepository
                        .findTokenByKey(authTokens[i].getAccessToken(), LogoutSessionAccessToken.class)).isNotNull();
                assertThat(tokenRepository
                        .findTokenByKey(authTokens[i].getRefreshToken(), LogoutSessionRefreshToken.class)).isNotNull();
            }

            else {
                Token accessToken = tokenRepository.findTokenByIdx(member.getUserId(), AccessToken.class);
                Token refreshToken = tokenRepository.findTokenByIdx(member.getUserId(), RefreshToken.class);

                assertThat(tokenRepository.findTokenByKey(accessToken.getId(), AccessToken.class)).isNotNull();
                assertThat(tokenRepository.findTokenByKey(refreshToken.getId(), RefreshToken.class)).isNotNull();
            }
        }

    }


}