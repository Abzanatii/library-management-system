package com.abz.library.reservation.controller;

import java.net.URI;

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

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/customer")
public class CustomerReservationController {

    private final ReservationService reservationService;

    @PostMapping ("/books/{id}/reserve")
    public ResponseEntity<ReservationResponse> reserve(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long bookId){
            ReservationResponse reservation = reservationService.reserve(user.id(), bookId);
            URI location =  ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/customer/reservations/{id}")
                            .buildAndExpand(reservation.id())
                            .toUri();
            return ResponseEntity.created(location).body(reservation);
    }

    @GetMapping ("/reservations")
    public PageResponse<ReservationResponse> findAllByCustomerId(
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestParam (required = false) ReservationStatus status,
        Pageable pageable){
            return reservationService.findAllByCustomerId(user.id(), status, pageable);
    }

    @GetMapping ("/reservations/{id}")
    public ReservationResponse findById(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return reservationService.findByIdAndCustomerId(id, user.id());
    }

    @PutMapping ("/reservations/{id}/cancel")
    public ReservationResponse cancel(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return reservationService.cancel(id, user.id());
    }
}
