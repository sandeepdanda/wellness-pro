package com.wellnesspro.service;

import com.wellnesspro.dto.AuthDtos.AuthResponse;
import com.wellnesspro.dto.AuthDtos.LoginRequest;
import com.wellnesspro.dto.AuthDtos.RegisterRequest;
import com.wellnesspro.exception.ApiExceptions.ConflictException;
import com.wellnesspro.model.Member;
import com.wellnesspro.repository.MemberRepository;
import com.wellnesspro.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new ConflictException("An account with that email already exists");
        }
        Member member = Member.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(Member.Role.MEMBER)
                .status(Member.MemberStatus.ACTIVE)
                .build();
        memberRepository.save(member);
        return toResponse(member);
    }

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException on failure, mapped to 401 by the handler.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        Member member = memberRepository.findByEmail(request.email()).orElseThrow();
        return toResponse(member);
    }

    private AuthResponse toResponse(Member member) {
        var principal = new User(
                member.getEmail(), member.getPassword(), List.of());
        String token = jwtService.generateToken(principal, member.getRole().name());
        return new AuthResponse(
                token, member.getId(), member.getName(), member.getEmail(), member.getRole().name());
    }
}
