package com.wellnesspro.service;

import com.wellnesspro.gateway.PaymentGateway;
import com.wellnesspro.gateway.PaymentResult;
import com.wellnesspro.model.Member;
import com.wellnesspro.model.MembershipPlan;
import com.wellnesspro.model.Payment;
import com.wellnesspro.repository.MemberRepository;
import com.wellnesspro.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Auto-renewal. Finds subscriptions whose period has ended, charges the plan price through
 * the {@link PaymentGateway}, and either extends the period (recording a COMPLETED payment)
 * or marks the member PAST_DUE (recording a FAILED payment). Never throws on a single
 * member's failure - one decline must not stop the batch.
 *
 * <p>Idempotent: a renewal only fires when {@code renewalDate <= today}, and a success
 * advances {@code renewalDate} by the plan duration, so re-running the same day is a no-op.
 * A failure leaves {@code renewalDate} unchanged so the next run retries (dunning).
 */
@Service
@RequiredArgsConstructor
public class RenewalService {

    private static final Logger log = LoggerFactory.getLogger(RenewalService.class);

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    /** Renews every subscription due as of {@code today}. Returns the number of successful renewals. */
    @Transactional
    public int renewDueSubscriptions(LocalDate today) {
        List<Member> due = memberRepository.findByMembershipPlanNotNullAndRenewalDateLessThanEqual(today);
        int renewed = 0;
        for (Member member : due) {
            if (renewOne(member, today)) {
                renewed++;
            }
        }
        if (!due.isEmpty()) {
            log.info("Auto-renewal run for {}: {} due, {} renewed", today, due.size(), renewed);
        }
        return renewed;
    }

    private boolean renewOne(Member member, LocalDate today) {
        MembershipPlan plan = member.getMembershipPlan();
        String description = plan.getName() + " plan renewal";
        PaymentResult result = paymentGateway.charge(member.getId(), plan.getPrice(), "CARD", description);

        if (result.success()) {
            paymentRepository.save(Payment.builder()
                    .member(member)
                    .amount(plan.getPrice())
                    .description(description)
                    .method("CARD")
                    .status(Payment.PaymentStatus.COMPLETED)
                    .build());
            member.setRenewalDate(member.getRenewalDate().plusMonths(plan.getDurationMonths()));
            member.setStatus(Member.MemberStatus.ACTIVE);
            memberRepository.save(member);
            return true;
        }

        paymentRepository.save(Payment.builder()
                .member(member)
                .amount(plan.getPrice())
                .description(description + " (declined)")
                .method("CARD")
                .status(Payment.PaymentStatus.FAILED)
                .build());
        member.setStatus(Member.MemberStatus.PAST_DUE);
        memberRepository.save(member);
        log.warn("Renewal declined for member {}: {}", member.getId(), result.failureReason());
        return false;
    }
}
