package com.abz.library.collection.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.abz.library.collection.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book>{

    boolean existsByDepartmentId(Long departmentId);

    boolean existsByAuthorsId(Long authorId);

    boolean existsByCategoriesId(Long categoryId);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbn(String isbn, Long id);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    @EntityGraph (attributePaths = {"department", "authors", "categories"})
    Optional<Book> findWithDetailsById(Long id);

    @EntityGraph (attributePaths = {"department", "authors", "categories"})
    List<Book> findWithDetailsByIdIn(List<Long> id);
}
