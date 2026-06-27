package com.wellnesspro.service;

import com.wellnesspro.dto.AnalyticsDtos.ClassOccupancy;
import com.wellnesspro.dto.AnalyticsDtos.DashboardSummary;
import com.wellnesspro.dto.AnalyticsDtos.LocationOccupancy;
import com.wellnesspro.model.Booking;
import com.wellnesspro.model.FitnessClass;
import com.wellnesspro.model.Member;
import com.wellnesspro.model.Payment;
import com.wellnesspro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MemberRepository memberRepository;
    private final FitnessClassRepository classRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public DashboardSummary buildSummary() {
        long totalMembers = memberRepository.count();
        long activeMembers = memberRepository.countByStatus(Member.MemberStatus.ACTIVE);
        long confirmedBookings = bookingRepository.countByStatus(Booking.BookingStatus.CONFIRMED);

        List<FitnessClass> classes = classRepository.findAll();
        long totalClasses = classes.size();

        BigDecimal totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double averageOccupancy = classes.stream()
                .filter(c -> c.getMaxCapacity() > 0)
                .mapToDouble(c -> 100.0 * c.getCurrentEnrollment() / c.getMaxCapacity())
                .average()
                .orElse(0.0);

        return new DashboardSummary(
                totalMembers, activeMembers, totalClasses, confirmedBookings,
                totalRevenue, round(averageOccupancy),
                occupancyByLocation(classes), topClasses(classes));
    }

    private List<LocationOccupancy> occupancyByLocation(List<FitnessClass> classes) {
        Map<Long, LocationOccupancy> byLocation = new LinkedHashMap<>();
        for (FitnessClass c : classes) {
            if (c.getLocation() == null) continue;
            Long locId = c.getLocation().getId();
            LocationOccupancy prev = byLocation.get(locId);
            int capacity = (prev != null ? prev.totalCapacity() : 0) + c.getMaxCapacity();
            int booked = (prev != null ? prev.booked() : 0) + c.getCurrentEnrollment();
            double pct = capacity > 0 ? round(100.0 * booked / capacity) : 0.0;
            byLocation.put(locId, new LocationOccupancy(
                    locId, c.getLocation().getName(), capacity, booked, pct));
        }
        return List.copyOf(byLocation.values());
    }

    private List<ClassOccupancy> topClasses(List<FitnessClass> classes) {
        return classes.stream()
                .map(c -> new ClassOccupancy(
                        c.getId(), c.getName(), c.getInstructor(),
                        c.getMaxCapacity(), c.getCurrentEnrollment(),
                        c.getMaxCapacity() > 0 ? round(100.0 * c.getCurrentEnrollment() / c.getMaxCapacity()) : 0.0))
                .sorted(Comparator.comparingDouble(ClassOccupancy::fillPct).reversed())
                .limit(5)
                .toList();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
