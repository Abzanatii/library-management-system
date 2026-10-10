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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/departments")
public class AdminDepartmentController {

    private final DepartmentService departmentService;

    @GetMapping 
    public List<DepartmentResponse> findAll(){
        return departmentService.findAll();
    }

    @GetMapping ("/{id}")
    public DepartmentResponse findById(@PathVariable Long id){
        return departmentService.findById(id);
    }

    @PostMapping 
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request){
        DepartmentResponse created = departmentService.create(request);
        URI location =  ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping ("/{id}")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request){
        return departmentService.update(id, request);
    }

    @DeleteMapping ("/{id}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        departmentService.delete(id);
    }
}
