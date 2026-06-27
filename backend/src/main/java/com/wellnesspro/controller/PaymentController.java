package com.wellnesspro.controller;

import com.wellnesspro.dto.Dtos.MemberResponse;
import com.wellnesspro.dto.Dtos.PaymentResponse;
import com.wellnesspro.dto.Dtos.SubscribeRequest;
import com.wellnesspro.security.CurrentMember;
import com.wellnesspro.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final SubscriptionService subscriptionService;
    private final CurrentMember currentMember;

    /** The authenticated member's payment history, newest first. */
    @GetMapping("/me")
    public List<PaymentResponse> myPayments(Authentication auth) {
        return subscriptionService.getPaymentsForMember(currentMember.requireId(auth));
    }

    /** Subscribe to a plan: records a payment and attaches the plan to the member. */
    @PostMapping("/subscribe")
    public MemberResponse subscribe(@Valid @RequestBody SubscribeRequest request, Authentication auth) {
        return subscriptionService.subscribe(currentMember.requireId(auth), request);
    }
}
