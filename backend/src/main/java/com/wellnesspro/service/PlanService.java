package com.wellnesspro.service;

import com.wellnesspro.dto.Dtos.PlanResponse;
import com.wellnesspro.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final MembershipPlanRepository planRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<PlanResponse> getAll() {
        return planRepository.findAll().stream().map(mapper::toPlan).toList();
    }
}
