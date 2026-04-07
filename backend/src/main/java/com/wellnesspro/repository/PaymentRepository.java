package com.wellnesspro.repository;

import com.wellnesspro.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMemberId(Long memberId);
}
