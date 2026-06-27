package com.wellnesspro.controller;

import com.wellnesspro.dto.Dtos.MemberResponse;
import com.wellnesspro.dto.Dtos.UpdateMemberRequest;
import com.wellnesspro.security.CurrentMember;
import com.wellnesspro.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final CurrentMember currentMember;

    /** The authenticated member's own profile. */
    @GetMapping("/me")
    public MemberResponse getMyProfile(Authentication auth) {
        return memberService.getByEmail(auth.getName());
    }

    @PatchMapping("/me")
    public MemberResponse updateMyProfile(@RequestBody UpdateMemberRequest request, Authentication auth) {
        return memberService.update(currentMember.requireId(auth), request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberResponse> getAllMembers() {
        return memberService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MemberResponse getMember(@PathVariable Long id) {
        return memberService.getById(id);
    }
}
