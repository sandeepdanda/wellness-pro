package com.wellnesspro.repository;

import com.wellnesspro.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByMemberId(Long memberId);
    List<Booking> findByFitnessClassId(Long classId);

    boolean existsByMemberIdAndFitnessClassIdAndStatus(
            Long memberId, Long classId, Booking.BookingStatus status);

    long countByStatus(Booking.BookingStatus status);
}
