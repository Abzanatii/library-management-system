package com.abz.library.borrowing.controller;

import org.springdoc.core.annotations.ParameterObject;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag (name = "Borrowing (customer)", description = "Logged in customer borrow, CUSTOMER only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/customer")
public class CustomerBorrowingController {

    private final BorrowingService borrowingService;

    @Operation (summary = "Brrow a book", description = """
    Customer borrowing rules:
    - Suspended/Blocked customers can not borrow
    - No active borrowing of the same book
    - Max ${borrowing.max-borrow} Active borrowings
    - Max ${borrowing.max-fines}EGP UNPAID fines 
    """)
    @PostMapping ("/borrow")
    @ResponseStatus (HttpStatus.CREATED)
    public BorrowingResponse borrow(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody BorrowingRequest request){
            return borrowingService.borrow(user.id(),request.bookId());
    }

    @Operation (summary = "Return a borrowed book", description = "Sets the copy to AVAILABLE, creates a fine of ${borrowing.per-day-fine}EGP per overdue day.")
    @PostMapping ("/borrowings/{id}/return")
    @ResponseStatus (HttpStatus.CREATED)
    public BorrowingResponse returnBorrow(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return borrowingService.returnByCustomer(id, user.id());
    }

    @Operation (summary = "List all logged in user borrowings", description = "Sort fields: {status, borrowedAt, returnedAt, dueDate}.")
    @GetMapping ("/borrowings")
    public PageResponse<BorrowingResponse> findAllByCustomerId(
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestParam (required = false) BorrowingStatus status,
        @ParameterObject Pageable pageable){
            return borrowingService.findAllByCustomerId(user.id(), status, pageable);
    }

    @Operation (summary = "List one borrowing owned by the user", description = "The borrowing must belong to the logged in user.")
    @GetMapping ("/borrowings/{id}")
    public BorrowingResponse findByIdAndCustomerId(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return borrowingService.findByIdAndCustomerId(id, user.id());
    }
}
