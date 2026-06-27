package com.wellnesspro.config;

import com.wellnesspro.model.*;
import com.wellnesspro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds demo data for the dev profile so the app is useful on first run.
 * Demo accounts: admin@wellnesspro.dev / member@wellnesspro.dev, password "password123".
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
    private final MembershipPlanRepository planRepository;
    private final FitnessClassRepository classRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            return; // already seeded
        }

        Location downtown = locationRepository.save(Location.builder()
                .name("Downtown Studio").address("100 Market St").capacity(120)
                .operatingHours("6:00 AM - 10:00 PM").build());
        Location riverside = locationRepository.save(Location.builder()
                .name("Riverside Retreat").address("42 Willow Rd").capacity(80)
                .operatingHours("5:30 AM - 9:00 PM").build());

        MembershipPlan monthly = planRepository.save(MembershipPlan.builder()
                .name("Monthly Flow").durationMonths(1).price(new BigDecimal("49.00"))
                .features("All classes, 1 location, app access").build());
        MembershipPlan annual = planRepository.save(MembershipPlan.builder()
                .name("Annual Zen").durationMonths(12).price(new BigDecimal("468.00"))
                .features("All classes, all locations, guest passes, priority booking").build());

        Member admin = memberRepository.save(Member.builder()
                .name("Ava Admin").email("admin@wellnesspro.dev")
                .password(passwordEncoder.encode("password123"))
                .phone("555-0001").role(Member.Role.ADMIN)
                .membershipPlan(annual).location(downtown)
                .renewalDate(LocalDate.now().minusDays(30).plusMonths(annual.getDurationMonths()))
                .status(Member.MemberStatus.ACTIVE).build());

        Member member = memberRepository.save(Member.builder()
                .name("Milo Member").email("member@wellnesspro.dev")
                .password(passwordEncoder.encode("password123"))
                .phone("555-0002").role(Member.Role.MEMBER)
                .membershipPlan(monthly).location(riverside)
                .renewalDate(LocalDate.now().minusDays(5).plusMonths(monthly.getDurationMonths()))
                .status(Member.MemberStatus.ACTIVE).build());

        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0);
        classRepository.saveAll(List.of(
                FitnessClass.builder().name("Sunrise Yoga").instructor("Priya N.").location(riverside)
                        .schedule(base).maxCapacity(20).currentEnrollment(12).build(),
                FitnessClass.builder().name("HIIT Burn").instructor("Marcus T.").location(downtown)
                        .schedule(base.plusHours(2)).maxCapacity(25).currentEnrollment(25).build(),
                FitnessClass.builder().name("Spin & Sweat").instructor("Lena K.").location(downtown)
                        .schedule(base.plusHours(4)).maxCapacity(30).currentEnrollment(18).build(),
                FitnessClass.builder().name("Restorative Stretch").instructor("Sam R.").location(riverside)
                        .schedule(base.plusDays(1)).maxCapacity(15).currentEnrollment(4).build()));

        paymentRepository.saveAll(List.of(
                Payment.builder().member(admin).amount(new BigDecimal("468.00"))
                        .description("Annual Zen plan")
                        .paymentDate(LocalDateTime.now().minusDays(30)).method("CARD")
                        .status(Payment.PaymentStatus.COMPLETED).build(),
                Payment.builder().member(member).amount(new BigDecimal("49.00"))
                        .description("Monthly Flow plan")
                        .paymentDate(LocalDateTime.now().minusDays(5)).method("CARD")
                        .status(Payment.PaymentStatus.COMPLETED).build()));
    }
}
