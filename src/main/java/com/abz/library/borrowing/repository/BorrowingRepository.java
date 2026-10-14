package com.abz.library.borrowing.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abz.library.borrowing.entity.Borrowing;
import com.abz.library.borrowing.entity.BorrowingStatus;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long>{

    boolean existsByCustomerIdAndBookCopy_Book_IdAndStatusIn(Long customerId, Long bookId, Collection<BorrowingStatus> statuses);

    long countByCustomerIdAndStatusIn(Long customerId, Collection<BorrowingStatus> statuses);
}
