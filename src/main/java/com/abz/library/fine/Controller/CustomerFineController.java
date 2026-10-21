package com.abz.library.fine.Controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.collection.dto.PageResponse;
import com.abz.library.fine.dto.FineResponse;
import com.abz.library.fine.entity.FineStatus;
import com.abz.library.fine.service.FineService;
import com.abz.library.security.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag (name = "Fines (customer)", description = "Fines payment by CUSTOMER only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/customer/fines")
public class CustomerFineController {

    private final FineService fineService;

    @Operation (summary = "Customer pays a fine", description = "Change UNPAID to PAID, there is no payment gateway.")
    @PostMapping ("/{id}/pay")
    @ResponseStatus (HttpStatus.CREATED)
    public FineResponse payment(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
            return fineService.payment(id, user.id());
    }

    @Operation (summary = "List all logged in user fines", description = "Sort fields: {status, amound, createdAt, paidAt}.")
    @GetMapping 
    public PageResponse<FineResponse> findAllById(
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestParam (required = false) FineStatus status,
        @ParameterObject Pageable pageable){
            return fineService.findAllbyId(user.id(), status, pageable);
    }

    @Operation (summary = "List one fine owned by the user", description = "The fine must belong to the logged in user.")
    @GetMapping ("/{id}")
    public FineResponse findByIdAndCustomerId(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable Long id){
        return fineService.findByIdAndCustomerId(id, user.id());
    }
}
