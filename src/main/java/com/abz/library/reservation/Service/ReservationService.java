package com.abz.library.reservation.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.borrowing.entity.BorrowingStatus;
import com.abz.library.borrowing.repository.BorrowingRepository;
import com.abz.library.collection.dto.PageResponse;
import com.abz.library.collection.entity.Book;
import com.abz.library.collection.entity.BookCopy;
import com.abz.library.collection.entity.CopyStatus;
import com.abz.library.collection.event.CopyAvailableEvent;
import com.abz.library.collection.repository.BookCopyRepository;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.collection.service.BookCopyService;
import com.abz.library.exception.BusinessRuleException;
import com.abz.library.exception.ResourceNotFoundException;
import com.abz.library.reservation.ReservationProperties;
import com.abz.library.reservation.Mapper.ReservationMapper;
import com.abz.library.reservation.Repository.ReservationRepository;
import com.abz.library.reservation.dto.ReservationResponse;
import com.abz.library.reservation.entity.Reservation;
import com.abz.library.reservation.entity.ReservationStatus;
import com.abz.library.user.entity.User;
import com.abz.library.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationProperties reservationProperties;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyService bookCopyService;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowingRepository borrowingRepository;

    private final Clock clock;
    private static final Set<String> SORTABLE = Set.of("status", "queuePosition", "reservedAt", "expiresAt");

    @Transactional 
    public ReservationResponse reserve(Long customerId, Long bookId){
        User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        customerIsActive(customer);
        isExpired();

        if (bookCopyRepository.findFirstByBookIdAndStatusOrderByCopyNumberAsc(bookId, CopyStatus.AVAILABLE).isPresent()) {
            throw new BusinessRuleException("This Book has avaiable copies to borrow");
        }
        if (borrowingRepository.existsByCustomerIdAndBookCopy_Book_IdAndStatusIn(
                                    customerId, bookId, BorrowingStatus.IS_BORROWED)) {
            throw new BusinessRuleException("Customer already has an active/overdue borrowing for this book");
        }
        if (reservationRepository.existsByCustomerIdAndBookIdAndStatusIn(customerId, bookId, ReservationStatus.IS_RESERVED)) {
            throw new BusinessRuleException("Customer already has a reserving for this book");
        }

        int position = reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WAITING) + 1;
        Reservation reservation =   Reservation.builder()
                                    .customer(customer)
                                    .book(book)
                                    .queuePosition(position)
                                    .reservedAt(LocalDateTime.now(clock))
                                    .build();
        return reservationMapper.toDetailedResponse(reservationRepository.save(reservation));
    }

    @Transactional 
    public ReservationResponse cancel(Long reservationId, Long customerId){
        Reservation reservation = reservationRepository.findWithDetailsById(reservationId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
        
        isExpired();
        if (!reservation.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Denied permission to modify this item");
        }

        boolean isready = reservation.getStatus() == ReservationStatus.READY;
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setQueuePosition(null);

        if (isready) {
            BookCopy copy = reservation.getBookCopy();
            bookCopyService.changeTo(copy, CopyStatus.AVAILABLE);
        }else{
            adjustQueue(reservation.getBook().getId());
        }

        return reservationMapper.toDetailedResponse(reservationRepository.save(reservation));
    }

    @Transactional (readOnly = true)
    private void adjustQueue(Long bookId){
        List<Reservation> waiting = reservationRepository
                                        .findByBookIdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.WAITING);
        int position = 1;
        for (Reservation reservation : waiting){
            reservation.setQueuePosition(position++);
        }
    }

    @Transactional 
    public void complete(Reservation reservation){
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.setQueuePosition(null);
    }

    @Transactional 
    @EventListener 
    public void returnedCopyListener(CopyAvailableEvent returnedBook){
        BookCopy copy = returnedBook.copy();
        Long bookId = copy.getBook().getId();

        Optional<Reservation> first = reservationRepository
            .findFirstByBookIdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.WAITING);
        if (first.isEmpty()) {
            return;
        }

        Reservation reservation = first.get();
        bookCopyService.changeTo(copy, CopyStatus.RESERVED);
        reservation.setStatus(ReservationStatus.READY);
        reservation.setBookCopy(copy);
        reservation.setQueuePosition(null);
        reservation.setExpiresAt(LocalDateTime.now(clock).plusHours(reservationProperties.expiryHours()));

        adjustQueue(bookId);
    }

    @Transactional 
    public Optional<Reservation> borrowReadyReservation(Long customerId, Long bookId){
        isExpired();
        return reservationRepository.findFirstByCustomerIdAndBookIdAndStatus(customerId, bookId, ReservationStatus.READY);
    }

    @Transactional 
    public void isExpired(){
        List<Reservation> expired = reservationRepository.findByStatusAndExpiresAtBefore
                                        (ReservationStatus.READY, LocalDateTime.now(clock));
        if (expired.isEmpty()) {
            return;
        }
        for (Reservation reservation : expired){
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservation.setQueuePosition(null);

            BookCopy copy = reservation.getBookCopy();
            bookCopyService.changeTo(copy, CopyStatus.AVAILABLE);
        }
        reservationRepository.saveAllAndFlush(expired);
    }

    @Transactional
    public  ReservationResponse findByIdAndCustomerId(Long reservationId, Long customerId){
        Reservation reservation = reservationRepository.findWithDetailsById(reservationId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
        
        if (!reservation.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Denied permission to view this item");
        }
        isExpired();
        return reservationMapper.toCustomerResponse(reservation);
    }

    @Transactional 
    public  PageResponse<ReservationResponse> findAllByCustomerId(Long customerId, ReservationStatus status, Pageable pageable){
        isExpired();
        Page<Reservation> page = (status == null) 
            ? reservationRepository.findByCustomerId(customerId, safeSort(pageable))
            : reservationRepository.findByCustomerIdAndStatus(customerId, status, safeSort(pageable));

        List<ReservationResponse> content = page.getContent().stream().map(reservationMapper::toCustomerResponse).toList();
        return new PageResponse<>(
            content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private void customerIsActive(User customer){
        switch (customer.getStatus()) {
            case BLOCKED -> throw new BusinessRuleException("Customer is currently 'BLOCKED'");
            case SUSPENDED -> throw new BusinessRuleException("Customer is currently 'SUSPENDED'");
            case ACTIVE -> {}
        }
    }

    private Pageable safeSort(Pageable pageable){
        Sort sort = pageable.getSort();
        for (Sort.Order order: sort){
            if (!SORTABLE.contains(order.getProperty())) {
                throw new BusinessRuleException("Sorting by: '" + order.getProperty() + "' is not allowed");
            }
        }
        if (sort.isUnsorted()) {
            sort = Sort.by(Reservation::getReservedAt);
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by(Reservation::getId)));
    }
}
