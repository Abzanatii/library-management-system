package com.abz.library.collection.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.collection.dto.DepartmentRequest;
import com.abz.library.collection.dto.DepartmentResponse;
import com.abz.library.collection.entity.Department;
import com.abz.library.collection.mapper.DepartmentMapper;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.collection.repository.DepartmentRepository;
import com.abz.library.exception.ConflictException;
import com.abz.library.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final BookRepository bookRepository;

    @Transactional 
    public DepartmentResponse create(DepartmentRequest request){
        String name = request.name();
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Department: '" + name + "' already exists");
        }
        Department newDepartment= departmentRepository.save(departmentMapper.toEntity(request));
        return departmentMapper.toResponse(newDepartment);
    }

    @Transactional 
    public DepartmentResponse update(Long id, DepartmentRequest request){
        Department existingDepartment = departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        String name = request.name();
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Department: '" + name + "' already exists");
        }
        departmentMapper.UpdateEntity(request, existingDepartment);
        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return departmentMapper.toResponse(updatedDepartment);
    }

    @Transactional 
    public void delete(Long id){
        Department department = departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        if (bookRepository.existsByDepartmentId(id)) {
            throw new ConflictException("Department: '" + department.getName() + "' still has recorded books");
        }
        departmentRepository.delete(department);
    }

    @Transactional (readOnly = true)
    public List<DepartmentResponse> findAll(){
        return  departmentRepository.findAll(Sort.by(Department::getName))
                .stream().map(departmentMapper::toResponse).toList();
    }

    @Transactional (readOnly = true)
    public DepartmentResponse findById(Long id){
        return  departmentMapper.toResponse(
                departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id)));
    }
}
