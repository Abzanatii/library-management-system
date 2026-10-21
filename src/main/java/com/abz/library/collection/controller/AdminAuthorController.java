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

import com.abz.library.collection.dto.AuthorRequest;
import com.abz.library.collection.dto.AuthorResponse;
import com.abz.library.collection.service.AuthorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag (name = "Authors (admin)", description = "ADMIN only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/authors")
public class AdminAuthorController {

    private final AuthorService authorService;

    @Operation (summary = "Create an author")
    @PostMapping
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request){
        AuthorResponse created = authorService.create(request);
        URI location =  ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation (summary = "List all authors")
    @GetMapping 
    public List<AuthorResponse> findAll(){
        return authorService.findAll();
    }

    @Operation (summary = "List an author")
    @GetMapping ("/{id}")
    public AuthorResponse findById(@PathVariable Long id){
        return authorService.findById(id);
    }

    @Operation (summary = "Update an author")
    @PutMapping ("/{id}")
    public AuthorResponse update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request){
        return authorService.update(id, request);
    }

    @Operation (summary = "Delete an author", description = "Not allowed while the author is linked to a book")
    @DeleteMapping ("/{id}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        authorService.delete(id);
    }
}
