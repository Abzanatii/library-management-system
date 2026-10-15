package com.abz.library.fine.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.abz.library.fine.dto.FineResponse;
import com.abz.library.fine.entity.Fine;

@Mapper (componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FineMapper {

    @Mapping (source = "borrowing.id", target = "borrowId")
    @Mapping (source = "borrowing.bookCopy.book.title", target = "bookTitle")
    FineResponse toResponse(Fine fine);
}
