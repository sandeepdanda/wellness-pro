package com.wellnesspro.controller;

import com.wellnesspro.dto.Dtos.PlanResponse;
import com.wellnesspro.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public List<PlanResponse> getAllPlans() {
        return planService.getAll();
    }
}
