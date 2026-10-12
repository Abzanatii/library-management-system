package com.abz.library.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abz.library.collection.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, Long>{

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
