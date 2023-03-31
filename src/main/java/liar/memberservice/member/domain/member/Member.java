package liar.memberservice.member.domain.member;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "member_email_index", columnList = "email"),
        @Index(name = "member_user_id_index", columnList = "userId")
})
public class Member extends BaseEntity implements Persistable {

    @Id
    @Column(name = "member_id")
    private String id;

    private String userId;

    private String registrationId;

    private String registerId;

    private String password;
    private String email;
    private String picture;

    @OneToMany(mappedBy = "member")
    private List<Authority> authorities = new ArrayList<>();

    private String username;


    @Builder
    public Member(String userId, String password, String registrationId, String registerId,
                  String email, String picture, String username) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.registrationId = registrationId;
        this.registerId = registerId;
        this.email = email;
        this.picture = picture;
    }

    public void addAuthorities(Authority authority) {
        this.authorities.add(authority);
        authority.updateUser(this);
    }

    /**
     * Returns if the {@code Persistable} is new or was persisted already.
     *
     * @return if {@literal true} the object is new.
     */
    @Override
    public boolean isNew() {
        return super.getCreatedAt() == null;
    }
}
