package com.abz.library.user.Controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.collection.dto.PageResponse;
import com.abz.library.user.dto.CustomerSearchCriteria;
import com.abz.library.user.dto.UserResponse;
import com.abz.library.user.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag (name = "Customers (admin)", description = "ADMIN only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/customers")
public class AdminCustomerController {

    private final CustomerService customerService;

    @Operation (summary = "List a customer")
    @GetMapping ("/{id}")
    public UserResponse findById(@PathVariable Long id){
        return customerService.findById(id);
    }

    @Operation (summary = "List all customers", description = "Searches by firstname, lastname and email, Sort fields: {firstName, lastName, email, status, createdAt}.")
    @GetMapping 
    public PageResponse<UserResponse> search(
        @ParameterObject CustomerSearchCriteria criteria, 
        @PageableDefault (size = 10, sort = "email", direction = Sort.Direction.ASC) 
        @ParameterObject Pageable pageable){
            return customerService.search(criteria, pageable);
    }

    @Operation (summary = "Suspend a customer", description = "Suspended customers can log in but can not borrow or reserve.")
    @PutMapping ("/{id}/suspend")
    public UserResponse suspend(@PathVariable Long id){
        return customerService.suspend(id);
    }

    @Operation (summary = "Activate a customer")
    @PutMapping ("/{id}/activate")
    public UserResponse activate(@PathVariable Long id){
        return customerService.activate(id);
    }
}
