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

import com.abz.library.collection.dto.CategoryRequest;
import com.abz.library.collection.dto.CategoryResponse;
import com.abz.library.collection.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag (name = "Categories (admin)", description = "ADMIN only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation (summary = "Create a category")
    @PostMapping 
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request){
        CategoryResponse created = categoryService.create(request);
        URI location =  ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation (summary = "Update a category")
    @PutMapping ("/{id}")
    public CategoryResponse update(@Valid @RequestBody CategoryRequest request, @PathVariable Long id){
        return categoryService.update(id, request);
    }

    @Operation (summary = "Delete a category", description = "Not allowed while the category is linked to a book.")
    @DeleteMapping ("/{id}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        categoryService.delete(id);
    }

    @Operation (summary = "List all categories")
    @GetMapping 
    public List<CategoryResponse> findAll(){
        return categoryService.findAll();
    }

    @Operation (summary = "List a category")
    @GetMapping ("/{id}")
    public CategoryResponse findById(@PathVariable Long id){
        return categoryService.findById(id);
    }
}
