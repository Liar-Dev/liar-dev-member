package liar.memberservice.member.repository.rdbms;

import jakarta.persistence.LockModeType;
import liar.memberservice.member.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.hibernate.LockMode.PESSIMISTIC_FORCE_INCREMENT;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    Member findMemberByUserId(String userId);
    Optional<Member> findByRegisterId(String registerId);
    Optional<Member> findByUserId(String userId);
    Member findByEmail(String email);
}
