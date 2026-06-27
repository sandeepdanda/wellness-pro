package com.wellnesspro.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response and request DTOs for the domain API. Response records never expose the
 * Member password hash (the JPA entities would, via Lombok @Data).
 */
public final class Dtos {

    private Dtos() {}

    public record LocationResponse(
            Long id, String name, String address, int capacity, String operatingHours) {}

    public record PlanResponse(
            Long id, String name, int durationMonths, BigDecimal price, String features) {}

    public record ClassResponse(
            Long id, String name, String instructor, Long locationId, String locationName,
            LocalDateTime schedule, int maxCapacity, int currentEnrollment, int availableSpots) {}

    public record MemberResponse(
            Long id, String name, String email, String phone, String role, String status,
            Long membershipPlanId, Long locationId, LocalDate joinDate) {}

    public record BookingResponse(
            Long id, Long memberId, Long classId, String className, String instructor,
            LocalDateTime classSchedule, LocalDateTime bookingDate, String status) {}

    public record CreateBookingRequest(
            @NotNull Long classId) {}

    public record CreateClassRequest(
            @NotNull String name,
            @NotNull String instructor,
            @NotNull Long locationId,
            @NotNull LocalDateTime schedule,
            @NotNull Integer maxCapacity) {}

    public record UpdateMemberRequest(
            String name, String phone, Long membershipPlanId, Long locationId) {}

    public record PaymentResponse(
            Long id, BigDecimal amount, LocalDateTime paymentDate, String method, String status, String description) {}

    public record SubscribeRequest(
            @NotNull Long planId,
            @NotNull String method) {}  // CARD, CASH, BANK_TRANSFER
}
