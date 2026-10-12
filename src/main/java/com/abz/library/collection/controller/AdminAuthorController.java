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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping (path = "/api/admin/authors")
public class AdminAuthorController {

    private final AuthorService authorService;

    @PostMapping
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request){
        AuthorResponse created = authorService.create(request);
        URI location =  ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.id())
                        .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping 
    public List<AuthorResponse> findAll(){
        return authorService.findAll();
    }

    @GetMapping ("/{id}")
    public AuthorResponse findById(@PathVariable Long id){
        return authorService.findById(id);
    }

    @PutMapping ("/{id}")
    public AuthorResponse update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request){
        return authorService.update(id, request);
    }

    @DeleteMapping ("/{id}")
    @ResponseStatus (HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        authorService.delete(id);
    }
}
