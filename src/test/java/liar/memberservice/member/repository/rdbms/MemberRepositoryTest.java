package liar.memberservice.member.repository.rdbms;

import liar.memberservice.member.domain.member.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired AuthorityRepository authorityRepository;

    @BeforeEach
    public void init() {
        authorityRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("member Save")
    public void member_saveTest() throws Exception {
        //given
        String email = "kose@naver.com";

        Member savedMember = memberRepository.save(
                Member.builder()
                .email(email)
                .password("1234")
                .username("kose")
                .build());

        //when
        Member findMember = memberRepository.findMemberByUserId(savedMember.getUserId());

        //then
        assertThat(savedMember.getId()).isEqualTo(findMember.getId());
    }


    @Test
    @DisplayName("단일 스레드 save")
    public void save_single() throws Exception {
        //given
        int result = 0;
        int count = 5;

        Member[] members = new Member[count];
        Member kose = Member.builder().email("kose@naver.com").password("1234").username("kose").build();

        //when
        for (int i = 0; i < count; i++) {
            try{
                Member member = memberRepository.findMemberByUserId(kose.getUserId());

                if (member == null) members[i] = memberRepository.save(kose);
                else members[i] = null;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < count; i++) if (members[i] != null) result++;

        //then
        assertThat(result).isEqualTo(1);

    }
}