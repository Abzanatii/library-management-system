package com.abz.library.collection.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.abz.library.collection.dto.BookCopyCreateRequest;
import com.abz.library.collection.dto.BookCopyResponse;
import com.abz.library.collection.entity.BookCopy;

@Mapper(componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookCopyMapper {

    @Mapping (target = "book", ignore = true)
    BookCopy toEntity(BookCopyCreateRequest bookCopyCreateRequest);
    
    @Mapping (target = "bookId", source = "book.id")
    @Mapping (target = "isbn", source = "book.isbn")
    @Mapping (target = "bookTitle", source = "book.title")
    BookCopyResponse toResponse(BookCopy bookCopy);
}
