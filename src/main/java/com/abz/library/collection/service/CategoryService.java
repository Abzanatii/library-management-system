package com.abz.library.collection.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.collection.dto.CategoryRequest;
import com.abz.library.collection.dto.CategoryResponse;
import com.abz.library.collection.entity.Category;
import com.abz.library.collection.mapper.CategoryMapper;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.collection.repository.CategoryRepository;
import com.abz.library.exception.ConflictException;
import com.abz.library.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final BookRepository bookRepository;

    @Transactional 
    public CategoryResponse create(CategoryRequest request){
        String name = request.name();
        if (categoryRepository.existsByNameIgnoreCase(name)){
            throw new ConflictException("Category: '" + name + "' already exists");
        }
        Category saved = categoryRepository.save(categoryMapper.toEntity(request));
        return categoryMapper.toResponse(saved);
    }

    @Transactional 
    public CategoryResponse update(Long id, CategoryRequest request){
        Category existingCategory = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
        String name = request.name();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)){
            throw new ConflictException("Category: '" + name + "' already exists");
        }
        categoryMapper.update(request, existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Transactional 
    public void delete(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (bookRepository.existsByCategoriesId(id)){
            throw new ConflictException("Category: '" + category.getName() + "' still has recorded books");
        }
        categoryRepository.delete(category);
    }

    @Transactional (readOnly = true)
    public List<CategoryResponse> findAll(){
        return  categoryRepository.findAll(Sort.by(Category::getName))
                .stream().map(categoryMapper::toResponse).toList();
    }

    @Transactional (readOnly = true)
    public CategoryResponse findById(Long id){
        return  categoryMapper.toResponse(
                categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id)));
    }
}
