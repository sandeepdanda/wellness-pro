package com.wellnesspro.service;

import com.wellnesspro.dto.Dtos.MemberResponse;
import com.wellnesspro.dto.Dtos.UpdateMemberRequest;
import com.wellnesspro.exception.ApiExceptions.NotFoundException;
import com.wellnesspro.model.Location;
import com.wellnesspro.model.Member;
import com.wellnesspro.model.MembershipPlan;
import com.wellnesspro.repository.LocationRepository;
import com.wellnesspro.repository.MemberRepository;
import com.wellnesspro.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final LocationRepository locationRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<MemberResponse> getAll() {
        return memberRepository.findAll().stream().map(mapper::toMember).toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse getById(Long id) {
        return mapper.toMember(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public MemberResponse getByEmail(String email) {
        return mapper.toMember(memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Member " + email + " not found")));
    }

    @Transactional
    public MemberResponse update(Long id, UpdateMemberRequest request) {
        Member member = findOrThrow(id);
        if (request.name() != null) member.setName(request.name());
        if (request.phone() != null) member.setPhone(request.phone());
        if (request.membershipPlanId() != null) {
            MembershipPlan plan = planRepository.findById(request.membershipPlanId())
                    .orElseThrow(() -> new NotFoundException("Plan " + request.membershipPlanId() + " not found"));
            member.setMembershipPlan(plan);
        }
        if (request.locationId() != null) {
            Location location = locationRepository.findById(request.locationId())
                    .orElseThrow(() -> new NotFoundException("Location " + request.locationId() + " not found"));
            member.setLocation(location);
        }
        return mapper.toMember(memberRepository.save(member));
    }

    private Member findOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member " + id + " not found"));
    }
}
