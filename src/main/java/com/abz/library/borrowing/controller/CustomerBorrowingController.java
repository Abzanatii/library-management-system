package com.abz.library.borrowing.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.borrowing.dto.BorrowingRequest;
import com.abz.library.borrowing.dto.BorrowingResponse;
import com.abz.library.borrowing.entity.BorrowingStatus;
import com.abz.library.borrowing.service.BorrowingService;
import com.abz.library.collection.dto.PageResponse;
import com.abz.library.security.AuthenticatedUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/customer")
public class CustomerBorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping ("/borrow")
    @ResponseStatus (HttpStatus.CREATED)
    public BorrowingResponse borrow(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody BorrowingRequest request){
            return borrowingService.borrow(user.id(),request.bookId());
    }

    @PostMapping ("/borrowings/{id}/return")
    @ResponseStatus (HttpStatus.CREATED)
    public BorrowingResponse returnBorrow(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return borrowingService.returnByCustomer(id, user.id());
    }

    @GetMapping ("/borrowings")
    public PageResponse<BorrowingResponse> findAllByCustomerId(
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestParam (required = false) BorrowingStatus status,
        Pageable pageable){
            return borrowingService.findAllByCustomerId(user.id(), status, pageable);
    }

    @GetMapping ("/borrowings/{id}")
    public BorrowingResponse findByIdAndCustomerId(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return borrowingService.findByIdAndCustomerId(id, user.id());
    }
}
