package com.wellnesspro.service;

import com.wellnesspro.dto.Dtos.MemberResponse;
import com.wellnesspro.dto.Dtos.PaymentResponse;
import com.wellnesspro.dto.Dtos.SubscribeRequest;
import com.wellnesspro.exception.ApiExceptions.BadRequestException;
import com.wellnesspro.exception.ApiExceptions.NotFoundException;
import com.wellnesspro.gateway.PaymentGateway;
import com.wellnesspro.gateway.PaymentResult;
import com.wellnesspro.model.Member;
import com.wellnesspro.model.MembershipPlan;
import com.wellnesspro.model.Payment;
import com.wellnesspro.repository.MemberRepository;
import com.wellnesspro.repository.MembershipPlanRepository;
import com.wellnesspro.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Member self-service subscription. Charges the plan price through the {@link PaymentGateway},
 * records the resulting payment, and attaches the plan to the member, all in one transaction.
 * The gateway is the seam for a real processor; the dev impl always approves.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final Set<String> METHODS = Set.of("CARD", "CASH", "BANK_TRANSFER");

    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForMember(Long memberId) {
        return paymentRepository.findByMemberIdOrderByPaymentDateDesc(memberId)
                .stream().map(mapper::toPayment).toList();
    }

    @Transactional
    public MemberResponse subscribe(Long memberId, SubscribeRequest request) {
        if (!METHODS.contains(request.method())) {
            throw new BadRequestException("method must be one of CARD, CASH, BANK_TRANSFER");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member " + memberId + " not found"));
        MembershipPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new NotFoundException("Plan " + request.planId() + " not found"));

        String description = plan.getName() + " plan";
        PaymentResult result = paymentGateway.charge(memberId, plan.getPrice(), request.method(), description);
        if (!result.success()) {
            throw new BadRequestException("Payment was declined: " + result.failureReason());
        }

        paymentRepository.save(Payment.builder()
                .member(member)
                .amount(plan.getPrice())
                .description(description)
                .method(request.method())
                .status(Payment.PaymentStatus.COMPLETED)
                .build());

        member.setMembershipPlan(plan);
        member.setRenewalDate(LocalDate.now().plusMonths(plan.getDurationMonths()));
        return mapper.toMember(memberRepository.save(member));
    }
}
