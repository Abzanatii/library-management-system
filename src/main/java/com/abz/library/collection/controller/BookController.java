package com.abz.library.collection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.collection.dto.BookResponse;
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
}
