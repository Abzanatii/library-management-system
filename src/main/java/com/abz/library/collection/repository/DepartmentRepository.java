package com.abz.library.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abz.library.collection.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long>{

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
