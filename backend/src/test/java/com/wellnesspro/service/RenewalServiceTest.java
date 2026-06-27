package com.wellnesspro.service;

import com.wellnesspro.gateway.PaymentGateway;
import com.wellnesspro.gateway.PaymentResult;
import com.wellnesspro.model.Member;
import com.wellnesspro.model.MembershipPlan;
import com.wellnesspro.model.Payment;
import com.wellnesspro.repository.MemberRepository;
import com.wellnesspro.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenewalServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PaymentGateway paymentGateway;
    RenewalService service;

    private MembershipPlan monthly;

    @BeforeEach
    void setUp() {
        service = new RenewalService(memberRepository, paymentRepository, paymentGateway);
        monthly = MembershipPlan.builder().id(1L).name("Monthly Flow").durationMonths(1)
                .price(new BigDecimal("49.00")).features("a").build();
    }

    private Member memberDueOn(LocalDate renewalDate) {
        return Member.builder().id(7L).name("Milo").email("m@wp.dev")
                .role(Member.Role.MEMBER).status(Member.MemberStatus.ACTIVE)
                .membershipPlan(monthly).renewalDate(renewalDate).build();
    }

    @Test
    void notDue_membersAreNotChargedOrChanged() {
        // The query only returns members whose renewalDate <= today, so a not-due member
        // never appears - the service charges nobody.
        when(memberRepository.findByMembershipPlanNotNullAndRenewalDateLessThanEqual(any()))
                .thenReturn(List.of());

        int renewed = service.renewDueSubscriptions(LocalDate.now());

        assertThat(renewed).isZero();
        verifyNoInteractions(paymentGateway);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void successPath_recordsCompletedPayment_andExtendsPeriod() {
        LocalDate today = LocalDate.of(2026, 6, 26);
        Member member = memberDueOn(today.minusDays(1));
        when(memberRepository.findByMembershipPlanNotNullAndRenewalDateLessThanEqual(today))
                .thenReturn(List.of(member));
        when(paymentGateway.charge(anyLong(), any(), anyString(), anyString()))
                .thenReturn(PaymentResult.success("SIM-123"));

        int renewed = service.renewDueSubscriptions(today);

        assertThat(renewed).isEqualTo(1);
        assertThat(member.getRenewalDate()).isEqualTo(today.minusDays(1).plusMonths(1));
        assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(saved.getAmount()).isEqualByComparingTo("49.00");
        assertThat(saved.getDescription()).contains("renewal");
        verify(memberRepository).save(member);
    }

    @Test
    void failurePath_recordsFailedPayment_doesNotExtend_marksPastDue() {
        LocalDate today = LocalDate.of(2026, 6, 26);
        LocalDate originalRenewal = today.minusDays(1);
        Member member = memberDueOn(originalRenewal);
        when(memberRepository.findByMembershipPlanNotNullAndRenewalDateLessThanEqual(today))
                .thenReturn(List.of(member));
        when(paymentGateway.charge(anyLong(), any(), anyString(), anyString()))
                .thenReturn(PaymentResult.failure("card_declined"));

        int renewed = service.renewDueSubscriptions(today);

        assertThat(renewed).isZero();
        assertThat(member.getRenewalDate()).isEqualTo(originalRenewal); // unchanged - retried next run
        assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.PAST_DUE);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
    }

    @Test
    void idempotent_rerunSameDay_doesNotDoubleCharge() {
        LocalDate today = LocalDate.of(2026, 6, 26);
        Member member = memberDueOn(today.minusDays(1));
        when(memberRepository.findByMembershipPlanNotNullAndRenewalDateLessThanEqual(today))
                // First run: member is due. After success, renewalDate moves into the future,
                // so the second run's query returns nothing for the same date.
                .thenReturn(List.of(member))
                .thenReturn(List.of());
        when(paymentGateway.charge(anyLong(), any(), anyString(), anyString()))
                .thenReturn(PaymentResult.success("SIM-123"));

        service.renewDueSubscriptions(today);
        service.renewDueSubscriptions(today);

        // Charged exactly once across both runs.
        verify(paymentGateway, times(1)).charge(anyLong(), any(), anyString(), anyString());
        verify(paymentRepository, times(1)).save(any());
    }
}
