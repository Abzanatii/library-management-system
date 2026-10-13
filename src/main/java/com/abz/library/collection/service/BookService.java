package com.abz.library.collection.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.collection.dto.BookRequest;
import com.abz.library.collection.dto.BookResponse;
import com.abz.library.collection.dto.BookSearchCriteria;
import com.abz.library.collection.dto.PageResponse;
import com.abz.library.collection.entity.Author;
import com.abz.library.collection.entity.Book;
import com.abz.library.collection.entity.Category;
import com.abz.library.collection.entity.Department;
import com.abz.library.collection.mapper.BookMapper;
import com.abz.library.collection.repository.AuthorRepository;
import com.abz.library.collection.repository.BookCopyRepository;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.collection.repository.CategoryRepository;
import com.abz.library.collection.repository.DepartmentRepository;
import com.abz.library.collection.specification.BookSpecs;
import com.abz.library.exception.BusinessRuleException;
import com.abz.library.exception.ConflictException;
import com.abz.library.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final BookCopyRepository bookCopyRepository;

    private static final Set<String> SORTABLE =
        Set.of("isbn", "title", "language", "publicationYear", "createdAt", "updatedAt");
    
    @Transactional 
    public BookResponse create(BookRequest request){

        Book book = bookMapper.toEntity(request);
        String isbn = normalizeIsbn(request.isbn());
        if (bookRepository.existsByIsbn(isbn)) {
            throw new ConflictException("Book with ISBN: '" + isbn + "' already exists");
        }

        Department department = departmentRepository.findById(request.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));
        Set<Author> authors = fetch(request.authorIds(), authorRepository, Author::getId, "Author");
        Set<Category> categories = fetch(request.categoryIds(), categoryRepository, Category::getId, "Category");

        book.setIsbn(isbn);
        book.setDepartment(department);
        book.setAuthors(authors);
        book.setCategories(categories);
        Book saved = bookRepository.save(book);
        return bookMapper.toResponse(saved);
    }

    @Transactional 
    public BookResponse update(Long id, BookRequest request){
        Book existingBook = bookRepository.findWithDetailsById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        String isbn = normalizeIsbn(request.isbn());
        if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new ConflictException("Book with ISBN: '" + isbn + "' already exists");
        }
        
        Department department = departmentRepository.findById(request.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));
        Set<Author> authors = fetch(request.authorIds(), authorRepository, Author::getId, "Author");
        Set<Category> categories = fetch(request.categoryIds(), categoryRepository, Category::getId, "Category");

        bookMapper.update(request, existingBook);
        existingBook.setIsbn(isbn);
        existingBook.setDepartment(department);
        existingBook.getAuthors().clear();
        existingBook.getAuthors().addAll(authors);
        existingBook.getCategories().clear();
        existingBook.getCategories().addAll(categories);
        return bookMapper.toResponse(bookRepository.saveAndFlush(existingBook));
    }

    @Transactional 
    public void delete(Long id){
        Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        if (bookCopyRepository.existsByBookId(id)) {
            throw new ConflictException("Book: '" + book.getTitle() + "' still has recorded copies");
        }
        bookRepository.delete(book);
    }

    @Transactional (readOnly = true)
    public BookResponse findById(Long id){
        return  bookMapper.toResponse(
                bookRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id)));
    }

    private static String normalizeIsbn(String rawIsbn){
        String isbn = rawIsbn.replaceAll("[\\s-]", "").toUpperCase();
        if(!isbn.matches("\\d{13}|\\{9}[\\dX]")){
            throw new BusinessRuleException("ISBN must be 10 or 13 digits");
        }
        return isbn;
    }

    @Transactional (readOnly = true)
    private <T> Set<T> fetch(
        Set<Long> ids, JpaRepository<T, Long> repository, Function<T, Long> getId, String resource){
            
            if (ids == null || ids.isEmpty()){
                return new HashSet<>();
            }

            List<T> found = repository.findAllById(ids);
            if (found.size() != ids.size()){
                Set<Long> foundIds = found.stream().map(getId).collect(Collectors.toSet());
                List<Long> missing = ids.stream().filter(id -> !foundIds.contains(id)).sorted().toList();
                throw new ResourceNotFoundException(resource + " ID(s) not found: " + missing);
            }
            return new HashSet<>(found);
    }

    @Transactional (readOnly = true)
    public PageResponse<BookResponse> search(BookSearchCriteria criteria, Pageable pageable){
        Page<Book> page = bookRepository.findAll(BookSpecs.from(criteria), safeSort(pageable));
    
        List<Long> ids = page.getContent().stream().map(Book::getId).toList();
        Map<Long, Book> withDetails = ids.isEmpty() ? Map.of() 
            : bookRepository.findWithDetailsByIdIn(ids).stream().collect(Collectors.toMap(Book::getId, Function.identity()));

        List<BookResponse> content = ids.stream().map(withDetails::get).map(bookMapper::toResponse).toList();
        return new PageResponse<>(content, 
                        page.getNumber(), 
                        page.getSize(), 
                        page.getTotalElements(), 
                        page.getTotalPages());
    }

    private Pageable safeSort(Pageable pageable){
        Sort sort = pageable.getSort();
        for (Sort.Order order: sort){
            if (!SORTABLE.contains(order.getProperty())) {
                throw new BusinessRuleException("Sorting by: '" + order.getProperty() + "' is not allowed");
            }
        }
        if (sort.isUnsorted()) {
            sort = Sort.by(Book::getTitle);
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by(Book::getId)));
    }
}
