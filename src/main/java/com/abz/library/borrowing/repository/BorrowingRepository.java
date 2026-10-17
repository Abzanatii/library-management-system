package com.abz.library.borrowing.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.abz.library.borrowing.entity.Borrowing;
import com.abz.library.borrowing.entity.BorrowingStatus;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long>{

    boolean existsByCustomerIdAndBookCopy_Book_IdAndStatusIn(Long customerId, Long bookId, Collection<BorrowingStatus> statuses);

    long countByCustomerIdAndStatusIn(Long customerId, Collection<BorrowingStatus> statuses);

    @EntityGraph (attributePaths = {"customer", "bookCopy", "bookCopy.book"})
    Optional<Borrowing> findWithDetailsById(Long id);

    @EntityGraph (attributePaths = {"customer", "bookCopy", "bookCopy.book"})
    Page<Borrowing> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph (attributePaths = {"customer", "bookCopy", "bookCopy.book"})
    Page<Borrowing> findByCustomerIdAndStatus(Long customerId, BorrowingStatus status, Pageable pageable);

    long countByStatus(BorrowingStatus status);

    @Query("""
            SELECT b.id AS bookId, b.isbn AS isbn, b.title AS title, COUNT(bor) AS countBorrow
            FROM Borrowing bor
            JOIN bor.bookCopy c
            JOIN c.book b
            GROUP BY b.id, b.isbn, b.title
            ORDER BY count(bor) desc, b.id""")
    List<MostBorrowed> findMostBorrowed(Pageable pageable);
    public interface MostBorrowed{
        Long getBookId();
        String getIsbn();
        String getTitle();
        long getCountBorrow();
    }
}
