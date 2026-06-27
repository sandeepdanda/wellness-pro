package com.wellnesspro.security;

import com.wellnesspro.model.Member;
import com.wellnesspro.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Resolves the authenticated Member from the SecurityContext (principal username is email). */
@Component
@RequiredArgsConstructor
public class CurrentMember {

    private final MemberRepository memberRepository;

    public Member require(Authentication authentication) {
        String email = authentication.getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    public Long requireId(Authentication authentication) {
        return require(authentication).getId();
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
