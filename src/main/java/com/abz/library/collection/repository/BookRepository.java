package com.abz.library.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abz.library.collection.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long>{

    boolean existsByDepartmentId(Long departmentId);
}
