package com.abz.library.collection.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.abz.library.collection.dto.DepartmentRequest;
import com.abz.library.collection.dto.DepartmentResponse;
import com.abz.library.collection.service.DepartmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag (name = "Department (Admin)", description = "ADMIN only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/departments")
public class AdminDepartmentController {

    private final DepartmentService departmentService;

    @Operation (summary = "List all departments")
    @GetMapping 
    public List<DepartmentResponse> findAll(){
        return departmentService.findAll();
    }

    @Operation (summary = "List a department")
    @GetMapping ("/{id}")
    public DepartmentResponse findById(@PathVariable Long id){
        return departmentService.findById(id);
    }

    @Operation (summary = "Create a department")
    @PostMapping 
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request){
        DepartmentResponse created = departmentService.create(request);
        URI location =  ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation (summary = "Update a department")
    @PutMapping ("/{id}")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request){
        return departmentService.update(id, request);
    }

    @Operation (summary = "Delete a department", description = "Not allowed while the department still has books.")
    @DeleteMapping ("/{id}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        departmentService.delete(id);
    }
}
