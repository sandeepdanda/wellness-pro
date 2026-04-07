package com.wellnesspro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private BigDecimal amount;
    private LocalDateTime paymentDate = LocalDateTime.now();
    private String method;  // CARD, CASH, BANK_TRANSFER

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.COMPLETED;

    public enum PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }
}
