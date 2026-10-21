package com.abz.library.collection.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.abz.library.collection.dto.BookRequest;
import com.abz.library.collection.dto.BookResponse;
import com.abz.library.collection.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag (name = "Books (admin)", description = "ADMIN only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/books")
public class AdminBookController {

    private final BookService bookService;

    @Operation (summary = "Create a book")
    @PostMapping 
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request){
        BookResponse created = bookService.create(request);
        URI location =  ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/books/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation (summary = "Update a book")
    @PutMapping ("/{id}")
    public BookResponse update(@Valid @RequestBody BookRequest request, @PathVariable Long id){
        return bookService.update(id, request);
    }

    @Operation (summary = "Delete a book", description = "Not allowed while the book still has copies.")
    @DeleteMapping ("/{id}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        bookService.delete(id);
    }
}
