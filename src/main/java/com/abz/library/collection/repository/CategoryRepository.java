package com.abz.library.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abz.library.collection.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
