package liar.memberservice.member.domain.member;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Authority extends BaseEntity implements Persistable {

    @Id
    @Column(name = "authority_id")
    private String id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(STRING)
    private Authorities authorities;

    @Builder
    public Authority(Member member, Authorities authorities) {
        this.id = UUID.randomUUID().toString();
        this.member = member;
        this.authorities = authorities;
    }

    public void updateUser(Member member) {
        this.member = member;
    }

    @Override
    public boolean isNew() {
        return super.getCreatedAt() == null;
    }
}
