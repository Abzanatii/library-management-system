package com.abz.library.collection.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.collection.dto.BookCopyCreateRequest;
import com.abz.library.collection.dto.BookCopyResponse;
import com.abz.library.collection.dto.BookCopyUpdateRequest;
import com.abz.library.collection.entity.Book;
import com.abz.library.collection.entity.BookCopy;
import com.abz.library.collection.entity.CopyStatus;
import com.abz.library.collection.mapper.BookCopyMapper;
import com.abz.library.collection.repository.BookCopyRepository;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.exception.BusinessRuleException;
import com.abz.library.exception.ConflictException;
import com.abz.library.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookCopyMapper bookCopyMapper;
    private final BookRepository bookRepository;

    @Transactional 
    public BookCopyResponse create(BookCopyCreateRequest request){
        Book book = bookRepository.findById(request.bookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", request.bookId()));
        
        String CopyNumber = normalizaCopy(request.copyNumber());
        if (bookCopyRepository.existsByBookIdAndCopyNumber(book.getId(), CopyNumber)) {
            throw new ConflictException("Copy: '" + CopyNumber + "' already exists for this book");
        }
        BookCopy copy = bookCopyMapper.toEntity(request);
        copy.setBook(book);
        copy.setCopyNumber(CopyNumber);
        return bookCopyMapper.toResponse(bookCopyRepository.save(copy));
    }

    @Transactional 
    public BookCopyResponse update(BookCopyUpdateRequest request, Long id){
        BookCopy copy = bookCopyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Book-Copy", id));

        String CopyNumber = normalizaCopy(request.copyNumber());
        if (bookCopyRepository.existsByBookIdAndCopyNumberAndIdNot(copy.getBook().getId(), CopyNumber, id)) {
            throw new ConflictException("Copy: '" + CopyNumber + "' already exists for this book");
        }

        copy.setCopyNumber(CopyNumber);
        staffChangeTo(copy, request.status());
        return bookCopyMapper.toResponse(bookCopyRepository.saveAndFlush(copy));
    }

    @Transactional (readOnly = true)
    public List<BookCopyResponse> findByBookId(Long bookId){
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", bookId);
        }
        return  bookCopyRepository.findByBookIdOrderByCopyNumberAsc(bookId)
                .stream().map(bookCopyMapper::toResponse).toList();
    }

    @Transactional (readOnly = true)
    public BookCopyResponse findById(Long id){
        return  bookCopyMapper.toResponse(
                bookCopyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book-Copy", id)));
    }

    private void staffChangeTo(BookCopy bookCopy, CopyStatus next){
        CopyStatus current = bookCopy.getStatus();
        if (current == next) {
            return;
        }
        if (current.isServiceManaged() || next.isServiceManaged()) {
            throw new BusinessRuleException("BORROWED and RESERVED are managed by the borrowing and reservation services");
        }
        if (!current.staffChangeTo(next)) {
            throw new BusinessRuleException("Status '" + current + "' change to '" + next + "' is not allowed");
        }
        bookCopy.setStatus(next);
    }

    @Transactional 
    public void changeTo(BookCopy copy, CopyStatus next){
        if (!copy.getStatus().changeTo(next)) {
            throw new BusinessRuleException("Status '" + copy.getStatus() + 
                "' change to '" + next + "' is not allowed");
        }
        copy.setStatus(next);
    }

    private static String normalizaCopy(String rawCopy){
        return rawCopy.trim().toUpperCase();
    }
}
