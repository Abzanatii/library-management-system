package com.abz.library.collection.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.abz.library.collection.dto.AuthorRequest;
import com.abz.library.collection.dto.AuthorResponse;
import com.abz.library.collection.entity.Author;

@Mapper (componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {

    Author toEntity(AuthorRequest authorRequest);

    void update(AuthorRequest authorRequest, @MappingTarget Author author);

    AuthorResponse toResponse(Author author);
}
