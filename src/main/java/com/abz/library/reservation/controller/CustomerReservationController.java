package com.abz.library.reservation.controller;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.abz.library.collection.dto.PageResponse;
import com.abz.library.reservation.Service.ReservationService;
import com.abz.library.reservation.dto.ReservationResponse;
import com.abz.library.reservation.entity.ReservationStatus;
import com.abz.library.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag (name = "Reservation (customer)", description = "Reserve unavailable books, CUSTOMER only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/customer")
public class CustomerReservationController {

    private final ReservationService reservationService;

    @Operation (summary = "Reserve book", description = """
    Reserve a book that does not have available copies to borrow,
    when the copy is AVAILABLE, the first in queue customer gets to borrow it 
    before the reservation expires after 48 hours.""")
    @PostMapping ("/books/{id}/reserve")
    public ResponseEntity<ReservationResponse> reserve(
        @Parameter (hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            ReservationResponse reservation = reservationService.reserve(user.id(), id);
            URI location =  ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/customer/reservations/{id}")
                            .buildAndExpand(reservation.id())
                            .toUri();
            return ResponseEntity.created(location).body(reservation);
    }

    @Operation (summary = "List all logged in user reservations", 
        description = "Sort fields: {status, queuePosition, reservedAt, expiresAt}")
    @GetMapping ("/reservations")
    public PageResponse<ReservationResponse> findAllByCustomerId(
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestParam (required = false) ReservationStatus status,
        @ParameterObject Pageable pageable){
            return reservationService.findAllByCustomerId(user.id(), status, pageable);
    }

    @Operation (summary = "List one reservation owned by the user", 
        description = "The reservation must belong to the logged in user")
    @GetMapping ("/reservations/{id}")
    public ReservationResponse findById(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return reservationService.findByIdAndCustomerId(id, user.id());
    }

    @Operation (summary = "Cancel reservation", 
        description = "Set a reservation to 'CANCELLED' and leaves the queue, a ready reservation frees the reserved copy.")
    @PutMapping ("/reservations/{id}/cancel")
    public ReservationResponse cancel(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return reservationService.cancel(id, user.id());
    }
}
