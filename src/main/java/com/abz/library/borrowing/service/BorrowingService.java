package com.abz.library.borrowing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.borrowing.BorrowingProperties;
import com.abz.library.borrowing.dto.BorrowingResponse;
import com.abz.library.borrowing.entity.Borrowing;
import com.abz.library.borrowing.entity.BorrowingStatus;
import com.abz.library.borrowing.mapper.BorrowingMapper;
import com.abz.library.borrowing.repository.BorrowingRepository;
import com.abz.library.collection.dto.PageResponse;
import com.abz.library.collection.entity.BookCopy;
import com.abz.library.collection.entity.CopyStatus;
import com.abz.library.collection.repository.BookCopyRepository;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.collection.service.BookCopyService;
import com.abz.library.exception.BusinessRuleException;
import com.abz.library.exception.ResourceNotFoundException;
import com.abz.library.fine.entity.FineStatus;
import com.abz.library.fine.repository.FineRepository;
import com.abz.library.fine.service.FineService;
import com.abz.library.user.entity.Role;
import com.abz.library.user.entity.User;
import com.abz.library.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class BorrowingService {

    private final BorrowingRepository borrowingRepository; 
    private final BorrowingMapper borrowingMapper;
    private final BorrowingProperties borrowingProperties;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final FineRepository fineRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookCopyService bookCopyService;
    private final FineService fineService;

    private final Clock clock;

    private final Set<BorrowingStatus> IS_BORROWED = EnumSet.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE);
    private final Set<String> SORTABLE = Set.of("status", "borrowedAt", "returnedAt", "dueDate");

    @Transactional 
    public BorrowingResponse borrow(Long customerId, Long bookId){
        User customer = userRepository.findByIdAndRole(customerId, Role.CUSTOMER)
                            .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", bookId);
        }
        BookCopy copy = bookCopyRepository.findFirstByBookIdAndStatusOrderByCopyNumberAsc(bookId, CopyStatus.AVAILABLE)
                            .orElseThrow(() -> new BusinessRuleException("No avaiable copies"));
        
        customerIsActive(customer);
        notBorrowed(customerId, bookId);
        belowMaxBorrow(customerId);
        belowMaxUnpaidFines(customerId);
        bookCopyService.changeTo(copy, CopyStatus.BORROWED);

        LocalDateTime now = LocalDateTime.now(clock);
        Borrowing borrowing = Borrowing.builder()
            .customer(customer)
            .bookCopy(copy)
            .borrowedAt(now)
            .dueDate(now.plusDays(borrowingProperties.PeriodDays()))
            .build();
        return borrowingMapper.toResponse(borrowingRepository.save(borrowing));
    }

    @Transactional (readOnly = true)
    public PageResponse<BorrowingResponse> findAllByCustomerId(Long customerId, BorrowingStatus status, Pageable pageable){
        Page<Borrowing> page = (status == null) 
            ? borrowingRepository.findByCustomerId(customerId, safeSort(pageable))
            : borrowingRepository.findByCustomerIdAndStatus(customerId, status, safeSort(pageable));

        List<BorrowingResponse> content = page.getContent().stream().map(borrowingMapper::toResponse).toList();
        return new PageResponse<>(
            content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional (readOnly = true)
    public BorrowingResponse findByIdAndCustomerId(Long borrowId, Long customerId){
        Borrowing borrow = borrowingRepository.findWithDetailsById(borrowId)
                            .orElseThrow(() -> new ResourceNotFoundException("Borrowing", borrowId));
        if (!borrow.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Denied permission to view this item");
        }
        return borrowingMapper.toResponse(borrow);
    }

    @Transactional 
    public BorrowingResponse returned(Borrowing borrowing, CopyStatus afterReturn){
        isBorrowed(borrowing);

        LocalDateTime now = LocalDateTime.now(clock);
        borrowing.setReturnedAt(now);
        borrowing.setStatus(BorrowingStatus.RETURNED);

        long overdue = ChronoUnit.DAYS.between(borrowing.getDueDate(), now);
        if (overdue > borrowingProperties.PeriodDays()) {
            BigDecimal fine = BigDecimal.valueOf(overdue).multiply(borrowingProperties.perDayFine())
                                .setScale(2, RoundingMode.HALF_UP);
            fineService.create(borrowing, fine, now);
            borrowing.setFineAmount(fine);
        }
        bookCopyService.changeTo(borrowing.getBookCopy(), afterReturn);
        return borrowingMapper.toResponse(borrowing);
    }

    @Transactional 
    public BorrowingResponse returnByLibrarian(Long borrowId, boolean damaged){
        return returned(borrowingRepository.findWithDetailsById(borrowId)
                            .orElseThrow(() -> new ResourceNotFoundException("Borrowing", borrowId)),
                        (damaged == true)
                            ? CopyStatus.DAMAGED : CopyStatus.AVAILABLE);
    }

    @Transactional 
    public BorrowingResponse returnByCustomer(Long borrowId, Long customerId){
        Borrowing borrow = borrowingRepository.findWithDetailsById(borrowId)
                            .orElseThrow(() -> new ResourceNotFoundException("Borrowing", borrowId));
        if (!borrow.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Denied permission to modify this item");
        }
        return returned(borrow, CopyStatus.AVAILABLE);
    }

    @Transactional 
    public BorrowingResponse setLost(Long borrowId){
        Borrowing borrow = borrowingRepository.findWithDetailsById(borrowId)
                                .orElseThrow(() -> new ResourceNotFoundException("Borrowing", borrowId));
        isBorrowed(borrow);
        borrow.setStatus(BorrowingStatus.LOST);
        bookCopyService.changeTo(borrow.getBookCopy(), CopyStatus.LOST);
        return borrowingMapper.toResponse(borrow);
    }

    private void isBorrowed(Borrowing borrowing){
        if (!IS_BORROWED.contains(borrowing.getStatus())) {
            throw new BusinessRuleException("Borrowing is not active, current status '" + borrowing.getStatus() + "'");
        }
    }

    private void customerIsActive(User customer){
        switch (customer.getStatus()) {
            case BLOCKED -> throw new BusinessRuleException("Customer is currently 'BLOCKED'");
            case SUSPENDED -> throw new BusinessRuleException("Customer is currently 'SUSPENDED'");
            case ACTIVE -> {}
        }
    }

    @Transactional (readOnly = true)
    private void notBorrowed(Long customerId, Long bookId){
        if (borrowingRepository.existsByCustomerIdAndBookCopy_Book_IdAndStatusIn(customerId, bookId, IS_BORROWED)) {
            throw new BusinessRuleException("Customer already has an active/overdue borrowing for this book");
        }
    }

    @Transactional (readOnly = true)
    private void belowMaxBorrow(Long customerId){
        if (borrowingRepository.countByCustomerIdAndStatusIn(customerId, IS_BORROWED) >= borrowingProperties.maxBorrow()) {
            throw new BusinessRuleException("Customer is not allowed to borrow over '" + 
                borrowingProperties.maxBorrow() + "' books");
        }
    }

    @Transactional (readOnly = true)
    private void belowMaxUnpaidFines(Long customerId){
        BigDecimal unpaidFines = fineRepository.sumAmountByCustomerIdAndStatus(customerId, FineStatus.UNPAID);
        unpaidFines = (unpaidFines == null) ? BigDecimal.ZERO : unpaidFines;
        if (unpaidFines.compareTo(borrowingProperties.maxFines()) > 0) {
            throw new BusinessRuleException("Customer above '" + borrowingProperties.maxFines() + " EGP' is not allowed to borrow books.\n Customer has unpaid fines: '" + unpaidFines.toPlainString() + " EGP'");
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
            sort = Sort.by(Borrowing::getBorrowedAt);
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by(Borrowing::getId)));
    }
}
