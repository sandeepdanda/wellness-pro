package com.wellnesspro.service;

import com.wellnesspro.dto.Dtos.*;
import com.wellnesspro.model.*;
import org.springframework.stereotype.Component;

/** Maps JPA entities to response DTOs. Keeps password hashes and lazy associations out of the API. */
@Component
public class DtoMapper {

    public LocationResponse toLocation(Location l) {
        return new LocationResponse(l.getId(), l.getName(), l.getAddress(), l.getCapacity(), l.getOperatingHours());
    }

    public PlanResponse toPlan(MembershipPlan p) {
        return new PlanResponse(p.getId(), p.getName(), p.getDurationMonths(), p.getPrice(), p.getFeatures());
    }

    public ClassResponse toClass(FitnessClass c) {
        int available = Math.max(0, c.getMaxCapacity() - c.getCurrentEnrollment());
        return new ClassResponse(
                c.getId(), c.getName(), c.getInstructor(),
                c.getLocation() != null ? c.getLocation().getId() : null,
                c.getLocation() != null ? c.getLocation().getName() : null,
                c.getSchedule(), c.getMaxCapacity(), c.getCurrentEnrollment(), available);
    }

    public MemberResponse toMember(Member m) {
        return new MemberResponse(
                m.getId(), m.getName(), m.getEmail(), m.getPhone(),
                m.getRole().name(), m.getStatus().name(),
                m.getMembershipPlan() != null ? m.getMembershipPlan().getId() : null,
                m.getLocation() != null ? m.getLocation().getId() : null,
                m.getJoinDate());
    }

    public PaymentResponse toPayment(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getAmount(), p.getPaymentDate(),
                p.getMethod(), p.getStatus().name(), p.getDescription());
    }

    public BookingResponse toBooking(Booking b) {
        FitnessClass c = b.getFitnessClass();
        return new BookingResponse(
                b.getId(),
                b.getMember() != null ? b.getMember().getId() : null,
                c != null ? c.getId() : null,
                c != null ? c.getName() : null,
                c != null ? c.getInstructor() : null,
                c != null ? c.getSchedule() : null,
                b.getBookingDate(), b.getStatus().name());
    }
}
