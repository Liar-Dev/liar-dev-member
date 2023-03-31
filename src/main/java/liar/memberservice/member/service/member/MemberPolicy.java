package liar.memberservice.member.service.member;

import liar.memberservice.common.principal.social.ProviderUser;
import liar.memberservice.exception.exception.NotFoundUserException;
import liar.memberservice.exception.exception.UserRegisterConflictException;
import liar.memberservice.member.domain.member.Authority;
import liar.memberservice.member.domain.member.Member;
import liar.memberservice.member.repository.rdbms.AuthorityRepository;
import liar.memberservice.member.repository.rdbms.MemberRepository;
import liar.memberservice.member.service.dto.FormRegisterUserDto;
import liar.memberservice.member.service.dto.LoginDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static liar.memberservice.member.domain.member.Authorities.ROLE_USER;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberPolicy {

    private final MemberRepository memberRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * findMethod
     */

    public List<Authority> findAuthorityByUserOrThrow(Member member) {
        List<Authority> authorities = authorityRepository.findAuthorityByMember(member);
        if (authorities.isEmpty()) throw new NotFoundUserException();
        return authorities;
    }

    public Member findByUserId(String userId) {
        return memberRepository.findByUserId(userId).orElseThrow(() -> {throw new NotFoundUserException();});
    }

    public Member findMemberByEmailOrThrow(LoginDto loginDto) {
        Member findUser = findMemberByEmail(loginDto.getEmail());
        if (findUser == null || !passwordEncoder.matches(loginDto.getPassword(), findUser.getPassword()))
            throw new NotFoundUserException();
        return findUser;
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Transactional
    public boolean register(FormRegisterUserDto dto) {
        Member findMember = memberRepository.findByEmail(dto.getEmail());

        if (findMember == null) {
            Member user = Member.builder()
                    .userId(UUID.randomUUID().toString())
                    .username(dto.getUsername())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .email(dto.getEmail())
                    .build();
            memberRepository.saveAndFlush(user);
            authorityRepository.saveAndFlush(Authority.builder().member(user).authorities(ROLE_USER).build());
            return true;
        }
        throw new UserRegisterConflictException();
    }

    /**
     * register
     */
    @Transactional
    public void register(String registrationId, ProviderUser providerUser) {
        Member savedMember = memberRepository.save(
                Member.builder()
                        .userId(UUID.randomUUID().toString())
                        .registrationId(registrationId)
                        .registerId(providerUser.getId())
                        .password(providerUser.getPassword())
                        .email(providerUser.getEmail())
                        .picture(providerUser.getPicture())
                        .build()
        );

        authorityRepository.save(
                Authority.builder().member(savedMember).authorities(ROLE_USER).build()
        );

    }

}
