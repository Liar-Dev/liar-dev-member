package liar.memberservice.member.repository.rdbms;

import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.member.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class AuthorityRepositoryTest {

    @Autowired AuthorityRepository authorityRepository;
    @Autowired MemberRepository memberRepository;

    @Test
    @DisplayName("findAuthority")
    public void findAuthority() throws Exception {
        //given
        String email = "kose@naver.com";

        Member savedMember = memberRepository.save(
                Member.builder()
                        .email(email)
                        .password("1234")
                        .username("kose")
                        .build());

        //when
        List<Authority> authorityByMember = authorityRepository.findAuthorityByMember(savedMember);

        //then
        Assertions.assertThat(authorityByMember.isEmpty()).isTrue();

    }

}