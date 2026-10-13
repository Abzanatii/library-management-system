package com.abz.library.collection.controller;

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

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/books")
public class BookController {

    private final BookService bookService;

    @GetMapping ("/{id}")
    public BookResponse findById(@PathVariable Long id){
        return bookService.findById(id);
    }

    @GetMapping 
    public PageResponse<BookResponse> search(BookSearchCriteria criteria, 
        @PageableDefault (size = 10, sort = "title", direction = Sort.Direction.ASC)
        Pageable pageable){
            return bookService.search(criteria, pageable);
    }
}
