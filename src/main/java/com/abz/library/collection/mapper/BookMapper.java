package com.abz.library.collection.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.abz.library.collection.dto.BookRequest;
import com.abz.library.collection.dto.BookResponse;
import com.abz.library.collection.entity.Book;

@Mapper(componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {DepartmentMapper.class, AuthorMapper.class, CategoryMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BookMapper {

    @Mapping (target = "isbn", ignore = true)
    @Mapping (target = "authors", ignore = true)
    @Mapping (target = "department", ignore = true)
    @Mapping (target = "categories", ignore = true)
    Book toEntity(BookRequest bookRequest);

    @Mapping (target = "isbn", ignore = true)
    @Mapping (target = "authors", ignore = true)
    @Mapping (target = "department", ignore = true)
    @Mapping (target = "categories", ignore = true)
    void update(BookRequest bookRequest, @MappingTarget Book book);


    BookResponse toResponse(Book book);
}
