package com.wellnesspro.service;

import com.wellnesspro.exception.ApiExceptions.ConflictException;
import com.wellnesspro.exception.ApiExceptions.NotFoundException;
import com.wellnesspro.model.Booking;
import com.wellnesspro.model.FitnessClass;
import com.wellnesspro.model.Member;
import com.wellnesspro.repository.BookingRepository;
import com.wellnesspro.repository.FitnessClassRepository;
import com.wellnesspro.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock FitnessClassRepository classRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks BookingService bookingService;

    private Member member;
    private FitnessClass openClass;

    @BeforeEach
    void setUp() {
        // Real mapper, not a mock - it has no collaborators worth stubbing.
        bookingService = new BookingService(bookingRepository, classRepository, memberRepository, new DtoMapper());
        member = Member.builder().id(1L).name("Milo").email("m@wp.dev").build();
        openClass = FitnessClass.builder().id(10L).name("Yoga").instructor("Priya")
                .schedule(LocalDateTime.now().plusDays(1)).maxCapacity(20).currentEnrollment(5).build();
    }

    @Test
    void book_succeeds_andIncrementsEnrollment() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(classRepository.findById(10L)).thenReturn(Optional.of(openClass));
        when(bookingRepository.existsByMemberIdAndFitnessClassIdAndStatus(1L, 10L, Booking.BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = bookingService.book(1L, 10L);

        assertThat(response.status()).isEqualTo("CONFIRMED");
        assertThat(openClass.getCurrentEnrollment()).isEqualTo(6);
        verify(classRepository).save(openClass);
    }

    @Test
    void book_rejectsFullClass() {
        openClass.setCurrentEnrollment(20);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(classRepository.findById(10L)).thenReturn(Optional.of(openClass));

        assertThatThrownBy(() -> bookingService.book(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("full");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void book_rejectsDoubleBooking() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(classRepository.findById(10L)).thenReturn(Optional.of(openClass));
        when(bookingRepository.existsByMemberIdAndFitnessClassIdAndStatus(1L, 10L, Booking.BookingStatus.CONFIRMED))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.book(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already have a booking");
    }

    @Test
    void book_rejectsClassThatAlreadyStarted() {
        openClass.setSchedule(LocalDateTime.now().minusHours(1));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(classRepository.findById(10L)).thenReturn(Optional.of(openClass));

        assertThatThrownBy(() -> bookingService.book(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already started");
    }

    @Test
    void book_unknownMember_throwsNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.book(1L, 10L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void cancel_freesSpot_andIsMemberScoped() {
        Booking booking = Booking.builder().id(99L).member(member).fitnessClass(openClass)
                .status(Booking.BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = bookingService.cancel(99L, 1L, false);

        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(openClass.getCurrentEnrollment()).isEqualTo(4);
    }

    @Test
    void cancel_rejectsOtherMembersBooking() {
        Booking booking = Booking.builder().id(99L).member(member).fitnessClass(openClass)
                .status(Booking.BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(99L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(99L, 2L, false))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("your own");
    }
}
