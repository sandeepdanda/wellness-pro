package com.wellnesspro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "members")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_plan_id")
    private MembershipPlan membershipPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private LocalDate joinDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.ACTIVE;

    public enum Role { MEMBER, ADMIN }
    public enum MemberStatus { ACTIVE, INACTIVE, SUSPENDED }
}
