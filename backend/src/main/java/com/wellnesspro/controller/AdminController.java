package com.wellnesspro.controller;

import com.wellnesspro.dto.AnalyticsDtos.DashboardSummary;
import com.wellnesspro.dto.Dtos.MemberResponse;
import com.wellnesspro.service.AnalyticsService;
import com.wellnesspro.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-only endpoints. Path is gated to ROLE_ADMIN in SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AnalyticsService analyticsService;
    private final MemberService memberService;

    @GetMapping("/analytics")
    public DashboardSummary analytics() {
        return analyticsService.buildSummary();
    }

    @GetMapping("/members")
    public List<MemberResponse> members() {
        return memberService.getAll();
    }
}
