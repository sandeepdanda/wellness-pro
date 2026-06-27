package com.wellnesspro;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellnesspro.model.FitnessClass;
import com.wellnesspro.model.Location;
import com.wellnesspro.model.Member;
import com.wellnesspro.model.MembershipPlan;
import com.wellnesspro.repository.BookingRepository;
import com.wellnesspro.repository.FitnessClassRepository;
import com.wellnesspro.repository.LocationRepository;
import com.wellnesspro.repository.MemberRepository;
import com.wellnesspro.repository.MembershipPlanRepository;
import com.wellnesspro.repository.PaymentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndBookingIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired LocationRepository locationRepository;
    @Autowired FitnessClassRepository classRepository;
    @Autowired BookingRepository bookingRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired MembershipPlanRepository planRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Long classId;
    private Long planId;

    @BeforeEach
    void seed() {
        // Delete in FK order so re-seeding between tests doesn't violate constraints.
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        classRepository.deleteAll();
        locationRepository.deleteAll();
        memberRepository.deleteAll();
        planRepository.deleteAll();
        Location loc = locationRepository.save(Location.builder()
                .name("Test Studio").address("1 Test").capacity(50).operatingHours("24/7").build());
        FitnessClass yoga = classRepository.save(FitnessClass.builder()
                .name("Yoga").instructor("P").location(loc)
                .schedule(LocalDateTime.now().plusDays(1)).maxCapacity(2).currentEnrollment(0).build());
        classId = yoga.getId();
        MembershipPlan plan = planRepository.save(MembershipPlan.builder()
                .name("Monthly Flow").durationMonths(1).price(new BigDecimal("49.00")).features("a,b").build());
        planId = plan.getId();
    }

    private String registerMember(String email) throws Exception {
        String body = """
                {"name":"Sub","email":"%s","password":"password123"}""".formatted(email);
        String res = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json").content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("token").asText();
    }

    @Test
    void register_login_book_flow() throws Exception {
        // Register returns 201 and a token; joinDate must be populated (Builder.Default fix).
        String registerBody = """
                {"name":"Ann","email":"ann@wp.dev","password":"password123","phone":"555"}""";
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json").content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(registerResponse).get("token").asText();

        // Profile does not leak the password and has a joinDate.
        mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.joinDate", notNullValue()));

        // Book the class -> 201.
        String bookBody = "{\"classId\":" + classId + "}";
        mockMvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(bookBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Double-book -> 409.
        mockMvc.perform(post("/api/bookings").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(bookBody))
                .andExpect(status().isConflict());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/bookings/me")).andExpect(status().isForbidden());
    }

    @Test
    void duplicateEmailRegistration_returnsConflict() throws Exception {
        String body = """
                {"name":"Bo","email":"dup@wp.dev","password":"password123"}""";
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void shortPassword_failsValidation() throws Exception {
        String body = """
                {"name":"Bo","email":"short@wp.dev","password":"123"}""";
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void subscribe_attachesPlan_andRecordsPayment() throws Exception {
        String token = registerMember("sub@wp.dev");
        String body = "{\"planId\":" + planId + ",\"method\":\"CARD\"}";

        mockMvc.perform(post("/api/payments/subscribe").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membershipPlanId").value(planId.intValue()));

        mockMvc.perform(get("/api/payments/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(49.00))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void memberCannotCreateClass() throws Exception {
        String token = registerMember("notadmin@wp.dev");
        String body = """
                {"name":"X","instructor":"Y","locationId":1,"schedule":"2099-01-01T10:00:00","maxCapacity":10}""";
        mockMvc.perform(post("/api/classes").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateClass() throws Exception {
        // Promote a registered member to ADMIN directly, then exercise the admin-only endpoint.
        registerMember("boss@wp.dev");
        Member boss = memberRepository.findByEmail("boss@wp.dev").orElseThrow();
        boss.setRole(Member.Role.ADMIN);
        memberRepository.save(boss);
        String token = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login").contentType("application/json")
                                .content("{\"email\":\"boss@wp.dev\",\"password\":\"password123\"}"))
                        .andReturn().getResponse().getContentAsString()).get("token").asText();

        String body = """
                {"name":"Pilates","instructor":"Z","locationId":%d,"schedule":"2099-01-01T10:00:00","maxCapacity":10}"""
                .formatted(locationRepository.findAll().get(0).getId());
        mockMvc.perform(post("/api/classes").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pilates"));
    }
}
