package com.wellnesspro.controller;

import com.wellnesspro.model.Booking;
import com.wellnesspro.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRepository bookingRepository;

    @GetMapping("/member/{memberId}")
    public List<Booking> getBookingsByMember(@PathVariable Long memberId) {
        return bookingRepository.findByMemberId(memberId);
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        // TODO: Check capacity, validate member, prevent double-booking
        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .map(b -> { b.setStatus(Booking.BookingStatus.CANCELLED); return ResponseEntity.ok(bookingRepository.save(b)); })
                .orElse(ResponseEntity.notFound().build());
    }
}
