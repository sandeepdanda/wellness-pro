package com.wellnesspro.controller;

import com.wellnesspro.dto.Dtos.BookingResponse;
import com.wellnesspro.dto.Dtos.CreateBookingRequest;
import com.wellnesspro.security.CurrentMember;
import com.wellnesspro.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final CurrentMember currentMember;

    /** A member's own bookings, derived from the token (no memberId in the path). */
    @GetMapping("/me")
    public List<BookingResponse> getMyBookings(Authentication auth) {
        return bookingService.getBookingsForMember(currentMember.requireId(auth));
    }

    @GetMapping("/member/{memberId}")
    public List<BookingResponse> getBookingsByMember(@PathVariable Long memberId, Authentication auth) {
        // Members may only read their own; admins may read anyone's.
        Long callerId = currentMember.requireId(auth);
        if (!currentMember.isAdmin(auth) && !callerId.equals(memberId)) {
            return bookingService.getBookingsForMember(callerId);
        }
        return bookingService.getBookingsForMember(memberId);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request, Authentication auth) {
        Long memberId = currentMember.requireId(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.book(memberId, request.classId()));
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long id, Authentication auth) {
        return bookingService.cancel(id, currentMember.requireId(auth), currentMember.isAdmin(auth));
    }
}
