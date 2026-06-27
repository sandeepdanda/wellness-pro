package com.wellnesspro.service;

import com.wellnesspro.dto.Dtos.SubscribeRequest;
import com.wellnesspro.exception.ApiExceptions.BadRequestException;
import com.wellnesspro.exception.ApiExceptions.NotFoundException;
import com.wellnesspro.gateway.SimulatedPaymentGateway;
import com.wellnesspro.model.Member;
import com.wellnesspro.model.MembershipPlan;
import com.wellnesspro.model.Payment;
import com.wellnesspro.repository.MemberRepository;
import com.wellnesspro.repository.MembershipPlanRepository;
import com.wellnesspro.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock MembershipPlanRepository planRepository;
    @Mock PaymentRepository paymentRepository;
    SubscriptionService service;

    private Member member;
    private MembershipPlan plan;

    @BeforeEach
    void setUp() {
        service = new SubscriptionService(memberRepository, planRepository, paymentRepository,
                new SimulatedPaymentGateway(), new DtoMapper());
        member = Member.builder().id(1L).name("Milo").email("m@wp.dev").role(Member.Role.MEMBER)
                .status(Member.MemberStatus.ACTIVE).build();
        plan = MembershipPlan.builder().id(5L).name("Annual Zen").durationMonths(12)
                .price(new BigDecimal("468.00")).features("a,b").build();
    }

    @Test
    void subscribe_recordsPayment_andAttachesPlan() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(planRepository.findById(5L)).thenReturn(Optional.of(plan));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.subscribe(1L, new SubscribeRequest(5L, "CARD"));

        assertThat(response.membershipPlanId()).isEqualTo(5L);
        assertThat(member.getMembershipPlan()).isEqualTo(plan);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualByComparingTo("468.00");
        assertThat(saved.getDescription()).contains("Annual Zen");
        assertThat(saved.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
    }

    @Test
    void subscribe_rejectsInvalidMethod() {
        assertThatThrownBy(() -> service.subscribe(1L, new SubscribeRequest(5L, "CRYPTO")))
                .isInstanceOf(BadRequestException.class);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void subscribe_unknownPlan_throwsNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(planRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.subscribe(1L, new SubscribeRequest(5L, "CARD")))
                .isInstanceOf(NotFoundException.class);
    }
}
