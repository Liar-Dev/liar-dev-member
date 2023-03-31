package liar.memberservice.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import liar.memberservice.exception.exception.UserRegisterConflictException;
import liar.memberservice.member.controller.dto.request.FormRegisterRequest;
import liar.memberservice.member.controller.dto.request.LoginFacadeRequest;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.member.domain.session.LoginSession;
import liar.memberservice.member.domain.token.*;
import liar.memberservice.member.repository.rdbms.AuthorityRepository;
import liar.memberservice.member.repository.rdbms.MemberRepository;
import liar.memberservice.member.repository.redis.LoginSessionRepository;
import liar.memberservice.member.repository.redis.TokenRepository;
import liar.memberservice.member.service.dto.AuthTokenDto;
import liar.memberservice.member.service.member.MemberPolicy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FacadeServiceTest {

    @Autowired FacadeService facadeService;
    @Autowired RedisTemplate redisTemplate;
    @Autowired MemberRepository memberRepository;
    @Autowired AuthorityRepository authorityRepository;
    @Autowired LoginSessionRepository loginSessionRepository;
    @Autowired TokenRepository tokenRepository;

    static int count = 5;
    static String commonEmail = "kose@naver.com";
    static String commonUsername = "gosekose";
    static String remoteAddr = "127.0.0.1";
    static String commonPassword = UUID.randomUUID().toString();


    @AfterEach
    public void tearDown() {
        redisTemplate.delete(redisTemplate.keys("*"));
        authorityRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("register의 성공을 singleThread를 테스트한다.")
    public void register_success_single() throws Exception {
        //given
        String email = "kose@naver.com";
        FormRegisterRequest request = new FormRegisterRequest(
                "gosekose", email, UUID.randomUUID().toString());

        //when
        facadeService.register(request);

        //then
        assertThat(memberRepository.findByEmail(email)).isNotNull();
    }


    @Test
    @DisplayName("register의 이미 존재하는 회원에 대한 singleThread를 테스트한다.")
    public void register_alreadyExist_single() throws Exception {
        //given
        String email = "kose@naver.com";
        initSavedEmail(email);

        //when
        FormRegisterRequest request = new FormRegisterRequest(
                "gosekose", email, UUID.randomUUID().toString());

        //then
        assertThatThrownBy(() -> {
            facadeService.register(request);}).isInstanceOf(UserRegisterConflictException.class);
    }

    private void initSavedEmail(String email) {
        FormRegisterRequest request = new FormRegisterRequest(
                "gosekose", email, UUID.randomUUID().toString());
        facadeService.register(request);
    }

    @Test
    @DisplayName("register에 대한 멀티 스레드 테스트를 한다.")
    public void register_mt() throws Exception {
        //given

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(count);

        //when
        for (int i = 0; i < count; i++) {
            int fidx = i;
            executorService.submit(() -> {
                try {
                    String email = "kose" + fidx + "@naver.com";
                    FormRegisterRequest request = new FormRegisterRequest(
                            "gosekose", email, UUID.randomUUID().toString());
                    facadeService.register(request);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        //then
        for (int i = 0; i < count; i++) {
            String email = "kose" + i + "@naver.com";
            assertThat(memberRepository.findByEmail(email)).isNotNull();
        }
    }

    @Test
    @DisplayName("login 싱글 스레드에서 테스트한다")
    public void login_succss_single() throws Exception {
        //given
        registerMember();

        //when
        AuthTokenDto loginTokens = facadeService.login(new LoginFacadeRequest(commonEmail, commonPassword, remoteAddr));

        //then
        assertThat(loginTokens.getAccessToken()).isNotNull();
        assertThat(loginTokens.getRefreshToken()).isNotNull();
        assertThat(loginTokens.getUserId()).isNotNull();
    }

    @Test
    @DisplayName("login을 멀티 스레드에서 테스트 한다")
    public void login_mt() throws Exception {
        //given
        registerMember();
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(count);

        //when
        AuthTokenDto[] authTokenDtos = new AuthTokenDto[count];
        for (int i = 0; i < count; i++) {
            int fIdx = i;
            executorService.submit(() -> {
               try {
                   authTokenDtos[fIdx] = facadeService
                           .login(LoginFacadeRequest.builder()
                           .email(commonEmail)
                           .password(commonPassword)
                           .remoteAddr("127.0.0." + fIdx).build());
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
        Thread.sleep(7000L);

        Member member = memberRepository.findByEmail(commonEmail);
        LoginSession nowLoginSession = loginSessionRepository.findLoginSessionByUserId(member.getUserId());
        int lastSessionIdx =  Integer.parseInt(nowLoginSession.getRemoteAddr().split("\\.")[3].trim());
        System.out.println("lastSessionIdx = " + lastSessionIdx);

        Thread.sleep(3000L);
        //then
        for (int i = 0; i < count; i++) {
            if (i != lastSessionIdx) {
                // 마지막 세션 idx가 아니면 액세스,리프레시 제거 , logoutSession 토큰 저장
                assertionsTokenResult(authTokenDtos, i, AccessToken.class, true);
                assertionsTokenResult(authTokenDtos, i, LogoutSessionAccessToken.class, false);
                assertionsTokenResult(authTokenDtos, i, RefreshToken.class, true);
                assertionsTokenResult(authTokenDtos, i, LogoutSessionRefreshToken.class, false);
            }

            else {
                // 마지막 세션이라면 토큰 저장, logoutSession 토큰 X
                // 토튼 인덱스와 로그인 토큰이 동일해야한다.
                assertionsTokenResult(authTokenDtos, i, LogoutSessionAccessToken.class, true);
                assertionsTokenResult(authTokenDtos, i, LogoutSessionRefreshToken.class, true);

                Token access = tokenRepository.findTokenByKey(authTokenDtos[i].getAccessToken(), AccessToken.class);
                Token refresh = tokenRepository.findTokenByKey(authTokenDtos[i].getRefreshToken(), RefreshToken.class);

                Token accessByIdx = tokenRepository.findTokenByIdx(member.getUserId(), AccessToken.class);
                Token refreshByIdx = tokenRepository.findTokenByIdx(member.getUserId(), RefreshToken.class);

                assertThat(accessByIdx.getId()).isEqualTo(access.getId());
                assertThat(refreshByIdx.getId()).isEqualTo(refresh.getId());
            }
        }

    }

    private void assertionsTokenResult(AuthTokenDto[] auths, int i, Class<?> clazz, boolean isNull) throws JsonProcessingException {
        String clazzName = clazz.getSimpleName();
        String tokenId = "";
        if (clazzName.contains("AccessToken"))  tokenId = auths[i].getAccessToken();
        else tokenId = auths[i].getRefreshToken();

        if (isNull) assertThat(tokenRepository.findTokenByKey(tokenId, clazz)).isNull();
        else assertThat(tokenRepository.findTokenByKey(tokenId, clazz)).isNotNull();
    }


    private void registerMember() {
        facadeService.register(FormRegisterRequest.builder()
                .email(commonEmail)
                .username(commonUsername)
                .password(commonPassword)
                .build());
    }

    private AuthTokenDto loginMember() {
        registerMember();
        AuthTokenDto dto = null;
        try {
            dto = facadeService.login(new LoginFacadeRequest(commonEmail, commonPassword, remoteAddr));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }



}