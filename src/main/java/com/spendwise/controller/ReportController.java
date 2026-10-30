package com.spendwise.controller;

import com.spendwise.dto.ApiResponse;
import com.spendwise.dto.CategorySpendingDto;
import com.spendwise.dto.MonthlyReportResponse;
import com.spendwise.dto.MonthlyTrendDto;
import com.spendwise.entity.User;
import com.spendwise.service.AuthService;
import com.spendwise.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    public ReportController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyReportResponse>> getMonthlyReport(
            @RequestParam(required = false) String month) {
        User user = authService.getCurrentUser();
        MonthlyReportResponse report = reportService.getMonthlyReport(user, month);
        return ResponseEntity.ok(ApiResponse.success(report, "Monthly report generated successfully"));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<ApiResponse<List<CategorySpendingDto>>> getCategoryBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = authService.getCurrentUser();
        List<CategorySpendingDto> breakdown = reportService.getCategoryBreakdown(user, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(breakdown, "Category breakdown generated successfully"));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<ApiResponse<List<MonthlyTrendDto>>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        User user = authService.getCurrentUser();
        List<MonthlyTrendDto> trend = reportService.getMonthlyTrends(user, months);
        return ResponseEntity.ok(ApiResponse.success(trend, "Monthly trend generated successfully"));
    }
}
