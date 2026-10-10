package com.abz.library.collection.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.abz.library.collection.dto.DepartmentRequest;
import com.abz.library.collection.dto.DepartmentResponse;
import com.abz.library.collection.entity.Department;

@Mapper (componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    Department toEntity(DepartmentRequest departmentRequest);

    void UpdateEntity(DepartmentRequest departmentRequest, @MappingTarget Department department);

    DepartmentResponse toResponse(Department department);
}
