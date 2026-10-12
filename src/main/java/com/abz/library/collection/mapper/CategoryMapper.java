package com.abz.library.collection.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.abz.library.collection.dto.CategoryRequest;
import com.abz.library.collection.dto.CategoryResponse;
import com.abz.library.collection.entity.Category;

@Mapper (componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    Category toEntity(CategoryRequest categoryRequest);

    void update(CategoryRequest categoryRequest, @MappingTarget Category category);

    CategoryResponse toResponse(Category category);
}
