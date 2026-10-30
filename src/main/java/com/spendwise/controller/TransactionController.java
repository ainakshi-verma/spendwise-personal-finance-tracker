package com.spendwise.controller;

import com.spendwise.dto.ApiResponse;
import com.spendwise.dto.TransactionRequest;
import com.spendwise.dto.TransactionResponse;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.service.AuthService;
import com.spendwise.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String sort) {

        User user = authService.getCurrentUser();

        String[] sortParams = sort.split(",");
        String sortProperty = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty).and(Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<TransactionResponse> result = transactionService.getTransactions(user, type, category, startDate, endDate, search, pageable);

        return ResponseEntity.ok(ApiResponse.success(result, "Transactions retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        TransactionResponse response = transactionService.getTransactionById(id, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction found"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        User user = authService.getCurrentUser();
        TransactionResponse response = transactionService.createTransaction(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transaction created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        User user = authService.getCurrentUser();
        TransactionResponse response = transactionService.updateTransaction(id, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Transaction deleted successfully"));
    }
}
