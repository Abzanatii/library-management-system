package com.abz.library.borrowing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.borrowing.dto.BorrowingResponse;
import com.abz.library.borrowing.dto.LibrarianBorrowingRequest;
import com.abz.library.borrowing.service.BorrowingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/librarian")
public class LibrarianBorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping ("/borrow")
    @ResponseStatus (HttpStatus.CREATED)
    public BorrowingResponse borrow(@Valid @RequestBody LibrarianBorrowingRequest request){
        return borrowingService.borrow(request.customerId(), request.bookId());
    }
}
