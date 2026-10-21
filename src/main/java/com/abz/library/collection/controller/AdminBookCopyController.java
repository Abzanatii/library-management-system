package com.abz.library.collection.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.abz.library.collection.dto.BookCopyCreateRequest;
import com.abz.library.collection.dto.BookCopyResponse;
import com.abz.library.collection.dto.BookCopyUpdateRequest;
import com.abz.library.collection.service.BookCopyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Tag (name = "Book-Copies (admin)", description = "A copy is one physical item of the book, ADMIN only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/book-copies")
public class AdminBookCopyController {

    private final BookCopyService bookCopyService;

    @Operation (summary = "Create a copy")
    @PostMapping 
    public ResponseEntity<BookCopyResponse> create(@Valid @RequestBody BookCopyCreateRequest request){
        BookCopyResponse created = bookCopyService.create(request);
        URI location =  ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation (summary = "Update a copy")
    @PutMapping ("/{id}")
    public BookCopyResponse update(@PathVariable Long id, @Valid @RequestBody BookCopyUpdateRequest request) {
        return bookCopyService.update(request, id);
    }

    @Operation (summary = "List a copy")
    @GetMapping ("/{id}")
    public BookCopyResponse findById(@PathVariable @Min (2) Long id){
        return bookCopyService.findById(id);
    }

    @Operation (summary = "List the copies of a book")
    @GetMapping 
    public List<BookCopyResponse> findByBookId(@RequestParam Long bookId){
        return bookCopyService.findByBookId(bookId);
    }
}
