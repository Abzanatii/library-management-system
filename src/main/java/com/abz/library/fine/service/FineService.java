package com.abz.library.fine.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.borrowing.entity.Borrowing;
import com.abz.library.collection.dto.PageResponse;
import com.abz.library.exception.BusinessRuleException;
import com.abz.library.exception.ResourceNotFoundException;
import com.abz.library.fine.Mapper.FineMapper;
import com.abz.library.fine.dto.FineResponse;
import com.abz.library.fine.entity.Fine;
import com.abz.library.fine.entity.FineStatus;
import com.abz.library.fine.repository.FineRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class FineService {

    private final FineRepository fineRepository;
    private final FineMapper fineMapper;
    private final Clock clock;

    private static final Set<String> SORTABLE = Set.of("status", "amount", "createdAt", "paidAt");

    @Transactional 
    public Fine create(Borrowing borrowing, BigDecimal fineAmount, LocalDateTime createdAt){
        Fine fine = Fine.builder()
                    .customer(borrowing.getCustomer())
                    .borrowing(borrowing)
                    .amount(fineAmount)
                    .createdAt(createdAt)
                    .build();
        return fineRepository.save(fine);
    }

    @Transactional 
    public FineResponse payment(Long fineId, Long customerId){
        Fine fine = fineRepository.findWithDetailsById(fineId)
                        .orElseThrow(() -> new ResourceNotFoundException("Fine", fineId));
        if (!fine.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Permission denied to modify this item");
        }
        switch (fine.getStatus()) {
            case PAID -> throw new BusinessRuleException("Fine already paid");
            case WAIVED -> throw new BusinessRuleException("Fine already waived");
            case UNPAID -> {} 
        }

        fine.setStatus(FineStatus.PAID);
        fine.setPaidAt(LocalDateTime.now(clock));
        return fineMapper.toResponse(fineRepository.saveAndFlush(fine));
    }

    @Transactional (readOnly = true)
    public FineResponse findByIdAndCustomerId(Long fineId, Long customerId){
        Fine fine = fineRepository.findWithDetailsById(fineId)
                        .orElseThrow(() -> new ResourceNotFoundException("Fine", fineId));
        if (!fine.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Permission denied to modify this item");
        }
        return fineMapper.toResponse(fine);
    }

    @Transactional (readOnly = true)
    public PageResponse<FineResponse> findAllbyId(Long customerId, FineStatus status, Pageable pageable){
        Page<Fine> page = (status == null)
                            ? fineRepository.findByCustomerId(customerId, safeSort(pageable))
                            : fineRepository.findByCustomerIdAndStatus(customerId, status, safeSort(pageable));
        List<FineResponse> content = page.getContent().stream().map(fineMapper::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private Pageable safeSort(Pageable pageable){
        Sort sort = pageable.getSort();
        for (Sort.Order order: sort){
            if (!SORTABLE.contains(order.getProperty())) {
                throw new BusinessRuleException("Sorting by: '" + order.getProperty() + "' is not allowed");
            }
        }
        if (sort.isUnsorted()) {
            sort = Sort.by(Fine::getCreatedAt);
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by(Fine::getId)));
    }
}
