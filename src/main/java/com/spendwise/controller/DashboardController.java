package com.spendwise.controller;

import com.spendwise.dto.ApiResponse;
import com.spendwise.dto.DashboardSummaryResponse;
import com.spendwise.dto.TransactionResponse;
import com.spendwise.entity.User;
import com.spendwise.service.AuthService;
import com.spendwise.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
            @RequestParam(required = false) String month) {
        User user = authService.getCurrentUser();
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(user, month);
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary loaded successfully"));
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecentTransactions() {
        User user = authService.getCurrentUser();
        List<TransactionResponse> recent = dashboardService.getRecentTransactions(user);
        return ResponseEntity.ok(ApiResponse.success(recent, "Recent transactions loaded successfully"));
    }
}
