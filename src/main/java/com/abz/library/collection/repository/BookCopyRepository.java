package com.abz.library.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abz.library.collection.entity.BookCopy;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long>{

    boolean existsByBookId(Long bookId);
}
