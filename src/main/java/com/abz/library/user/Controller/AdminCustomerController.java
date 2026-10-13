package com.abz.library.user.Controller;

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

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/customers")
public class AdminCustomerController {

    private final CustomerService customerService;

    @GetMapping ("/{id}")
    public UserResponse findById(@PathVariable Long id){
        return customerService.findById(id);
    }

    @GetMapping 
    public PageResponse<UserResponse> search(CustomerSearchCriteria criteria, 
        @PageableDefault (size = 10, sort = "email", direction = Sort.Direction.ASC) 
        Pageable pageable){
            return customerService.search(criteria, pageable);
    }

    @PutMapping ("/{id}/suspend")
    public UserResponse suspend(@PathVariable Long id){
        return customerService.suspend(id);
    }

    @PutMapping ("/{id}/activate")
    public UserResponse activate(@PathVariable Long id){
        return customerService.activate(id);
    }
}
