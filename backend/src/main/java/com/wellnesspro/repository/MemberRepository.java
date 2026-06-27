package com.wellnesspro.repository;

import com.wellnesspro.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByStatus(Member.MemberStatus status);

    /** Members whose subscription period ends on or before the given date - candidates for renewal. */
    List<Member> findByMembershipPlanNotNullAndRenewalDateLessThanEqual(LocalDate date);
}
