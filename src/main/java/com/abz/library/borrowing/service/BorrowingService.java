package com.abz.library.borrowing.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.borrowing.BorrowingProperties;
import com.abz.library.borrowing.dto.BorrowingResponse;
import com.abz.library.borrowing.entity.Borrowing;
import com.abz.library.borrowing.entity.BorrowingStatus;
import com.abz.library.borrowing.mapper.BorrowingMapper;
import com.abz.library.borrowing.repository.BorrowingRepository;
import com.abz.library.collection.entity.BookCopy;
import com.abz.library.collection.entity.CopyStatus;
import com.abz.library.collection.repository.BookCopyRepository;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.collection.service.BookCopyService;
import com.abz.library.exception.BusinessRuleException;
import com.abz.library.exception.ResourceNotFoundException;
import com.abz.library.fine.entity.FineStatus;
import com.abz.library.fine.repository.FineRepository;
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

    private final Clock clock;

    private final Set<BorrowingStatus> IS_BORROWED = EnumSet.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE);

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
        if (borrowingRepository.countByCustomerIdAndStatusIn(customerId, IS_BORROWED) > borrowingProperties.maxBorrow()) {
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
}
