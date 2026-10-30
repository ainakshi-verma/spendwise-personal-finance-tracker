package com.spendwise.controller;

import com.spendwise.dto.ApiResponse;
import com.spendwise.dto.BudgetRequest;
import com.spendwise.dto.BudgetResponse;
import com.spendwise.entity.User;
import com.spendwise.service.AuthService;
import com.spendwise.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final AuthService authService;

    public BudgetController(BudgetService budgetService, AuthService authService) {
        this.budgetService = budgetService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @RequestParam(required = false) String month) {
        if (month == null || month.trim().isEmpty()) {
            month = YearMonth.now().toString();
        }
        User user = authService.getCurrentUser();
        List<BudgetResponse> budgets = budgetService.getBudgetsForMonth(user, month);
        return ResponseEntity.ok(ApiResponse.success(budgets, "Budgets retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        BudgetResponse response = budgetService.getBudgetById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget found"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(@Valid @RequestBody BudgetRequest request) {
        User user = authService.getCurrentUser();
        BudgetResponse response = budgetService.createBudget(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Budget created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        User user = authService.getCurrentUser();
        BudgetResponse response = budgetService.updateBudget(id, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        budgetService.deleteBudget(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted successfully"));
    }
}
