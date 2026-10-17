package com.abz.library.collection.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.abz.library.collection.entity.BookCopy;
import com.abz.library.collection.entity.CopyStatus;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long>{

    boolean existsByBookId(Long bookId);

    boolean existsByBookIdAndCopyNumber(Long bookId, String copyNumber);

    boolean existsByBookIdAndCopyNumberAndIdNot(Long bookId, String copyNumber, Long id);

    @EntityGraph (attributePaths = "book")
    Optional<BookCopy> findWithBookById(Long id);

    @EntityGraph (attributePaths = "book")
    List<BookCopy> findByBookIdOrderByCopyNumberAsc(Long bookId);

    Optional<BookCopy> findFirstByBookIdAndStatusOrderByCopyNumberAsc(Long bookId, CopyStatus status);

    @Query ("SELECT c.status AS status, COUNT(c) AS total FROM BookCopy c GROUP BY status")
    List<StatusCount> countGroupedByStatus();
    public interface StatusCount {
        CopyStatus getStatus();
        Long getTotal();
    }
}
