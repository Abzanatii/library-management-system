package com.abz.library.report.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.report.dto.MostBorrowedResponse;
import com.abz.library.report.dto.OverviewReportResponse;
import com.abz.library.report.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag (name = "Reports (admin)", description = "ADMIN only")
@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    @Operation (summary = "Library overview report", description = "Gets data about the whole library.")
    @GetMapping ("/overview")
    public OverviewReportResponse overview(){
        return reportService.overview();
    }

    @Operation (summary = "Most Borrowed Books report",description = "Default limit set to 5 books.")
    @GetMapping ("most-borrowed-books")
    public List<MostBorrowedResponse> mostBorrowed(@RequestParam (defaultValue = "5") int limit){
        return reportService.mostBorrowed(limit);
    }
}
