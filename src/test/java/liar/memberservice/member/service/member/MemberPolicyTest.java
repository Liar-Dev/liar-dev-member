package liar.memberservice.member.service.member;

import liar.memberservice.member.repository.rdbms.AuthorityRepository;
import liar.memberservice.member.repository.rdbms.MemberRepository;
import liar.memberservice.member.service.dto.FormRegisterUserDto;
import liar.memberservice.exception.exception.UserRegisterConflictException;
import liar.memberservice.member.service.member.MemberPolicy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberPolicyTest {

    @Autowired
    MemberPolicy memberPolicy;
    @Autowired MemberRepository memberRepository;
    @Autowired AuthorityRepository authorityRepository;

    @AfterEach
    public void tearDown() {
        authorityRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("싱글 스레드에서 같은 email 저장 요청이 오면  맨처음 이외에 요청은 실패한다.")
    public void registerForm_single() throws Exception {
        //given
        int count = 5;
        FormRegisterUserDto dto = new FormRegisterUserDto("kose", "kose@naver.com", "12345678910");

        //when
        memberPolicy.register(dto);

        //then
        for (int i = 0; i < count; i++) {
            Assertions.assertThatThrownBy(() -> {
                memberPolicy.register(dto);
            }).isInstanceOf(UserRegisterConflictException.class);
        }
    }


    @Test
    @DisplayName("멀티 스레드 save")
    @Transactional
    public void save_mt() throws Exception {
        //given
        int result = 0;
        int threadCount = 5;
        boolean[] registers = new boolean[threadCount];

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        FormRegisterUserDto dto = new FormRegisterUserDto("kose", "kose@naver.com", "12345678910");

        //when
        for (int i = 0; i < threadCount; i++) {
            int finalIdx = i;
            executorService.submit(() -> {
                try{
                    registers[finalIdx] = memberPolicy.register(dto);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();


        for (int i = 0; i < threadCount; i++) {
            if (registers[i]) result++;
        }

        //then
        assertThat(result).isEqualTo(1);
    }

}