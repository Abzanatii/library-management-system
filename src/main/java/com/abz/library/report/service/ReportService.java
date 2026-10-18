package com.abz.library.report.service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abz.library.borrowing.entity.BorrowingStatus;
import com.abz.library.borrowing.repository.BorrowingRepository;
import com.abz.library.borrowing.service.BorrowingService;
import com.abz.library.collection.entity.CopyStatus;
import com.abz.library.collection.repository.BookCopyRepository;
import com.abz.library.collection.repository.BookRepository;
import com.abz.library.collection.repository.BookCopyRepository.StatusCount;
import com.abz.library.fine.entity.FineStatus;
import com.abz.library.fine.repository.FineRepository;
import com.abz.library.report.dto.MostBorrowedResponse;
import com.abz.library.report.dto.OverviewReportResponse;
import com.abz.library.reservation.Service.ReservationService;
import com.abz.library.user.entity.Role;
import com.abz.library.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ReportService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;
    private final FineRepository fineRepository;
    private final ReservationService reservationService;
    private final BorrowingService borrowingService;

    @Transactional 
    public OverviewReportResponse overview(){
        borrowingService.isOverdue();
        reservationService.isExpired();

        Map<CopyStatus, Long> copiesByStatus = new EnumMap<>(CopyStatus.class);
        long totalCopies = 0;
        for(StatusCount r : bookCopyRepository.countGroupedByStatus()){
            copiesByStatus.put(r.getStatus(), r.getTotal());
            totalCopies += r.getTotal();
        }
        BigDecimal unpaidFines = fineRepository.sumAmountBystatus(FineStatus.UNPAID);

        return new OverviewReportResponse(
            bookRepository.count(),
            totalCopies,
            copiesByStatus.getOrDefault(CopyStatus.AVAILABLE, 0L),
            copiesByStatus.getOrDefault(CopyStatus.BORROWED, 0L),
            copiesByStatus.getOrDefault(CopyStatus.DAMAGED, 0L),
            userRepository.countByRole(Role.CUSTOMER),
            borrowingRepository.countByStatus(BorrowingStatus.ACTIVE),
            borrowingRepository.countByStatus(BorrowingStatus.OVERDUE),
            (unpaidFines == null) ? BigDecimal.ZERO : unpaidFines);
    }

    @Transactional (readOnly = true)
    public List<MostBorrowedResponse> mostBorrowed(int limit){
        return borrowingRepository.findMostBorrowed(PageRequest.of(0, limit))
            .stream().map(r -> new MostBorrowedResponse(r.getBookId(), r.getIsbn(), r.getTitle(), r.getCountBorrow())).toList();
    }
}
