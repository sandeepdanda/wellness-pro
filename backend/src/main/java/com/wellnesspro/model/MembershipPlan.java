package com.wellnesspro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "membership_plans")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MembershipPlan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // e.g., "Monthly Basic", "Annual Premium"
    private int durationMonths;
    private BigDecimal price;
    private String features;      // comma-separated feature list
}
