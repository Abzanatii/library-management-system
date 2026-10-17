package com.abz.library.fine.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abz.library.fine.entity.Fine;
import com.abz.library.fine.entity.FineStatus;

public interface FineRepository extends JpaRepository<Fine, Long>{

    @Query ("SELECT SUM(f.amount) FROM Fine f WHERE f.customer.id = :customerId AND f.status = :status")
    BigDecimal sumAmountByCustomerIdAndStatus(@Param ("customerId") Long customerId, 
                                              @Param ("status") FineStatus status);

    @EntityGraph (attributePaths = {"customer", "borrowing", "borrowing.bookCopy", "borrowing.bookCopy.book"})
    Optional<Fine> findWithDetailsById(Long id);

    @EntityGraph (attributePaths = {"customer", "borrowing", "borrowing.bookCopy", "borrowing.bookCopy.book"})
    Page<Fine> findByCustomerId(Long customerId, Pageable pageable);
    
    @EntityGraph (attributePaths = {"customer", "borrowing", "borrowing.bookCopy", "borrowing.bookCopy.book"})
    Page<Fine> findByCustomerIdAndStatus(Long customerId, FineStatus status, Pageable pageable);
    
    @Query ("SELECT SUM(f.amount) FROM Fine f WHERE f.status = :status")
    BigDecimal sumAmountBystatus(@Param ("status") FineStatus status);
}
