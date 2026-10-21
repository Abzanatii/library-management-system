package com.abz.library.collection.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.collection.dto.BookResponse;
import com.abz.library.collection.dto.BookSearchCriteria;
import com.abz.library.collection.dto.PageResponse;
import com.abz.library.collection.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag (name = "Books", description = "Open to any logged in user")
@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/books")
public class BookController {

    private final BookService bookService;

    @Operation (summary = "List a book")
    @GetMapping ("/{id}")
    public BookResponse findById(@PathVariable Long id){
        return bookService.findById(id);
    }

    @Operation (summary = "Search books", 
        description = "Sort fields: {isbn, title, language, publicationYear, createdAt, updatedAt}.")
    @GetMapping 
    public PageResponse<BookResponse> search(
        @ParameterObject BookSearchCriteria criteria, 
        @PageableDefault (size = 10, sort = "title", direction = Sort.Direction.ASC)
        @ParameterObject Pageable pageable){
            return bookService.search(criteria, pageable);
    }
}
