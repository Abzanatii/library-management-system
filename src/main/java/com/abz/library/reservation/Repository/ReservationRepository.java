package com.abz.library.reservation.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abz.library.reservation.entity.Reservation;
import com.abz.library.reservation.entity.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long>{

    @EntityGraph (attributePaths = "bookCopy")
    List<Reservation> findByStatusAndExpiredAtBefore(ReservationStatus status, LocalDateTime time);

    boolean existsByCustomerIdAndBookIdAndStatusIn(Long customerId, Long bookId, Collection<ReservationStatus> statuses);

    int countByBookIdAndStatus(Long bookId, ReservationStatus status);

    @EntityGraph (attributePaths = {"customer", "book", "bookCopy"})
    Optional<Reservation> findWithDetailsById(Long id);

    List<Reservation> findByBookIdAndStatusOrderByQueuePositionAsc(Long bookId, ReservationStatus status);

    Optional<Reservation> findFirstByBookIdAndStatusOrderByQueuePositionAsc(Long bookId, ReservationStatus status);

    @EntityGraph (attributePaths = "bookCopy")
    Optional<Reservation> findFirstByCustomerIdAndBookIdAndStatus(Long customerId, Long bookId, ReservationStatus status);

    @EntityGraph (attributePaths = "book")
    Page<Reservation> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph (attributePaths = "book")
    Page<Reservation> findByCustomerIdAndStatus(Long customerId, ReservationStatus status, Pageable pageable);
}
