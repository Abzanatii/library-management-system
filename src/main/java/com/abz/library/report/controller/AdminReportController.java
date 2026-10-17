package com.abz.library.report.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abz.library.report.dto.MostBorrowedResponse;
import com.abz.library.report.dto.OverviewReportResponse;
import com.abz.library.report.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping ("/overview")
    public OverviewReportResponse overview(){
        return reportService.overview();
    }

    @GetMapping ("most-borrowed-books")
    public List<MostBorrowedResponse> mostBorrowed(@RequestParam (defaultValue = "5") int limit){
        return reportService.mostBorrowed(limit);
    }
}
