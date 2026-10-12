package com.abz.library.collection.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.collection.dto.AuthorRequest;
import com.abz.library.collection.dto.AuthorResponse;
import com.abz.library.collection.entity.Author;
import com.abz.library.collection.mapper.AuthorMapper;
import com.abz.library.collection.repository.AuthorRepository;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.exception.ConflictException;
import com.abz.library.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final BookRepository bookRepository;

    @Transactional 
    public AuthorResponse create(AuthorRequest request){
        String name = request.name();
        if (authorRepository.existsByNameIgnoreCase(name)){
            throw new ConflictException("Author: '" + name + "' already exists");
        }
        Author newAuthor = authorRepository.save(authorMapper.toEntity(request));
        return authorMapper.toResponse(newAuthor);
    }

    @Transactional 
    public AuthorResponse update(Long id, AuthorRequest request){
        Author existingAuthor = authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Author", id));
        String name = request.name();
        if (authorRepository.existsByNameIgnoreCaseAndIdNot(name, id)){
            throw new ConflictException("Author: '" + name + "' already exists");
        }
        authorMapper.update(request, existingAuthor);
        Author updatedAuthor = authorRepository.save(existingAuthor);
        return authorMapper.toResponse(updatedAuthor);
    }

    @Transactional 
    public void delete(Long id){
        Author author = authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Author", id));
        if (bookRepository.existsByAuthorsId(id)) {
            throw new ConflictException("Author: '" + author.getName() + "' still has recorded books");
        }
        authorRepository.delete(author);
    }

    @Transactional (readOnly = true)
    public List<AuthorResponse> findAll(){
        return  authorRepository.findAll(Sort.by(Author::getName))
                .stream().map(authorMapper::toResponse).toList();
    }

    @Transactional (readOnly = true)
    public AuthorResponse findById(Long id){
        return  authorMapper.toResponse(
                authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id)));
    }
}
