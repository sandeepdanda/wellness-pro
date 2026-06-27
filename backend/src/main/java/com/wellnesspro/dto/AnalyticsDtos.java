package com.wellnesspro.dto;

import java.math.BigDecimal;
import java.util.List;

/** Aggregated metrics for the admin dashboard. */
public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    public record DashboardSummary(
            long totalMembers,
            long activeMembers,
            long totalClasses,
            long confirmedBookings,
            BigDecimal totalRevenue,
            double averageOccupancyPct,
            List<LocationOccupancy> occupancyByLocation,
            List<ClassOccupancy> topClasses) {}

    public record LocationOccupancy(
            Long locationId, String locationName, int totalCapacity, int booked, double occupancyPct) {}

    public record ClassOccupancy(
            Long classId, String className, String instructor, int maxCapacity, int currentEnrollment, double fillPct) {}
}
