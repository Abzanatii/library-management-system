package com.abz.library.collection.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abz.library.collection.entity.BookCopy;
import com.abz.library.collection.entity.CopyStatus;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long>{

    boolean existsByBookId(Long bookId);

    boolean existsByBookIdAndCopyNumber(Long bookId, String copyNumber);

    boolean existsByBookIdAndCopyNumberAndIdNot(Long bookId, String copyNumber, Long id);

    Optional<BookCopy> findWithBookById(Long id);

    List<BookCopy> findByBookIdOrderByCopyNumberAsc(Long bookId);

    Optional<BookCopy> findFirstByBookIdAndStatusOrderByCopyNumberAsc(Long bookId, CopyStatus status);
}
