package com.abz.library.user.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.collection.dto.PageResponse;
import com.abz.library.exception.BusinessRuleException;
import com.abz.library.exception.ResourceNotFoundException;
import com.abz.library.user.dto.CustomerSearchCriteria;
import com.abz.library.user.dto.UserResponse;
import com.abz.library.user.entity.Role;
import com.abz.library.user.entity.User;
import com.abz.library.user.entity.UserStatus;
import com.abz.library.user.mapper.UserMapper;
import com.abz.library.user.repository.UserRepository;
import com.abz.library.user.specification.CustomerSpec;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class CustomerService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final Set<String> SORTABLE =
        Set.of("firstName", "lastName", "email", "status", "createdAt");

    @Transactional (readOnly = true)
    public PageResponse<UserResponse> search(CustomerSearchCriteria criteria, Pageable pageable){
        Page<User> page =  userRepository.findAll(CustomerSpec.from(criteria),safeSort(pageable));
        List<UserResponse> content = page.getContent().stream().map(userMapper::toResponse).toList();
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
            sort = Sort.by(User::getEmail);
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by(User::getId)));
    }

    @Transactional (readOnly = true)
    public UserResponse findById(Long id){
        return userMapper.toResponse(
                userRepository.findByIdAndRole(id, Role.CUSTOMER)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", id)));
    }

    @Transactional 
    public UserResponse suspend(Long id){
        User customer = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        if (customer.getStatus() == UserStatus.BLOCKED) {
            throw new BusinessRuleException("The customer is currently blocked, please activate first.");
        }
        customer.setStatus(UserStatus.SUSPENDED);
        return userMapper.toResponse(customer);
    }

    @Transactional 
    public UserResponse activate(Long id){
        User customer = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        customer.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(customer);
    }
}
