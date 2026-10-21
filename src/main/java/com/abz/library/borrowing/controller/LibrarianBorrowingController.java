package com.abz.library.borrowing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.borrowing.dto.BorrowingResponse;
import com.abz.library.borrowing.dto.LibrarianBorrowingRequest;
import com.abz.library.borrowing.service.BorrowingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag (name = "Borrowing (Librarian)", description = "Librarian or Admin borrowing request for a customer")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/librarian")
public class LibrarianBorrowingController {

    private final BorrowingService borrowingService;

    @Operation (summary = "Borrow a book for a customer", description = """
    Borrowing rules:
    - Suspended/Blocked customers can not borrow
    - No active borrowing of the same book
    - Max ${borrowing.max-borrow} Active borrowings
    - Max ${borrowing.max-fines}EGP UNPAID fines
    """)
    @PostMapping ("/borrow")
    @ResponseStatus (HttpStatus.CREATED)
    public BorrowingResponse borrow(@Valid @RequestBody LibrarianBorrowingRequest request){
        return borrowingService.borrow(request.customerId(), request.bookId());
    }

    @Operation (summary = "Return any borrowed book", description = "Add boolean filter 'damaged=true' to set the returned copy to 'DAMAGED' otherwise defaults to 'AVAILABLE'.")
    @PostMapping ("/borrowing/{id}/return")
    public BorrowingResponse returnBorrow(
        @PathVariable Long id,
        @RequestParam (required = false) boolean damaged){
            return borrowingService.returnByLibrarian(id, damaged);
    }

    @Operation (summary = "Set a borrowing to LOST", description = "The borrow and its copy both become LOST.")
    @PostMapping ("/borrowing/{id}/lost")
    public BorrowingResponse setLost(@PathVariable Long id){
        return borrowingService.setLost(id);
    }
}
