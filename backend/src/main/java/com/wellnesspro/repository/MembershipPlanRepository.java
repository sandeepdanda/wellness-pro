package com.wellnesspro.repository;

import com.wellnesspro.model.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
}
