package com.wellnesspro.service;

import com.wellnesspro.dto.Dtos.BookingResponse;
import com.wellnesspro.exception.ApiExceptions.ConflictException;
import com.wellnesspro.exception.ApiExceptions.NotFoundException;
import com.wellnesspro.model.Booking;
import com.wellnesspro.model.FitnessClass;
import com.wellnesspro.model.Member;
import com.wellnesspro.repository.BookingRepository;
import com.wellnesspro.repository.FitnessClassRepository;
import com.wellnesspro.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FitnessClassRepository classRepository;
    private final MemberRepository memberRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForMember(Long memberId) {
        return bookingRepository.findByMemberId(memberId).stream().map(mapper::toBooking).toList();
    }

    /**
     * Books a member into a class. Rejects when the class is full, already started,
     * or the member already holds a confirmed booking for it. Increments enrollment
     * atomically within the transaction.
     */
    @Transactional
    public BookingResponse book(Long memberId, Long classId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member " + memberId + " not found"));
        FitnessClass fitnessClass = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class " + classId + " not found"));

        if (fitnessClass.getSchedule() != null && fitnessClass.getSchedule().isBefore(LocalDateTime.now())) {
            throw new ConflictException("That class has already started");
        }
        if (fitnessClass.getCurrentEnrollment() >= fitnessClass.getMaxCapacity()) {
            throw new ConflictException("That class is full");
        }
        if (bookingRepository.existsByMemberIdAndFitnessClassIdAndStatus(
                memberId, classId, Booking.BookingStatus.CONFIRMED)) {
            throw new ConflictException("You already have a booking for that class");
        }

        fitnessClass.setCurrentEnrollment(fitnessClass.getCurrentEnrollment() + 1);
        classRepository.save(fitnessClass);

        Booking booking = Booking.builder()
                .member(member)
                .fitnessClass(fitnessClass)
                .bookingDate(LocalDateTime.now())
                .status(Booking.BookingStatus.CONFIRMED)
                .build();
        return mapper.toBooking(bookingRepository.save(booking));
    }

    /** Cancels a confirmed booking and frees its spot. Idempotent for already-cancelled bookings. */
    @Transactional
    public BookingResponse cancel(Long bookingId, Long requestingMemberId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking " + bookingId + " not found"));

        if (!isAdmin && !booking.getMember().getId().equals(requestingMemberId)) {
            throw new ConflictException("You can only cancel your own bookings");
        }
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            FitnessClass fitnessClass = booking.getFitnessClass();
            fitnessClass.setCurrentEnrollment(Math.max(0, fitnessClass.getCurrentEnrollment() - 1));
            classRepository.save(fitnessClass);
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
        return mapper.toBooking(booking);
    }
}
