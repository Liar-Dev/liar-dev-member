package liar.memberservice.member.service.dto;

import liar.memberservice.member.domain.member.Member;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberInfoDto {

    private String email;
    private String username;

    public MemberInfoDto(Member member) {
        this.email = member.getEmail();
        this.username = member.getUsername();
    }

}
