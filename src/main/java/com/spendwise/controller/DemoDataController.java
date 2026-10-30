package com.spendwise.controller;

import com.spendwise.dto.ApiResponse;
import com.spendwise.entity.User;
import com.spendwise.service.AuthService;
import com.spendwise.service.DemoDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoDataController {

    private final DemoDataService demoDataService;
    private final AuthService authService;

    public DemoDataController(DemoDataService demoDataService, AuthService authService) {
        this.demoDataService = demoDataService;
        this.authService = authService;
    }

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedDemoData() {
        User user = authService.getCurrentUser();
        int count = demoDataService.seedDemoData(user);
        boolean alreadyLoaded = (count == 0);
        String message = alreadyLoaded
                ? "Demo data is already loaded for your account."
                : String.format("Successfully loaded %d sample transactions and category budgets.", count);

        return ResponseEntity.ok(ApiResponse.success(
            Map.of("insertedTransactions", count, "alreadyLoaded", alreadyLoaded),
            message
        ));
    }

    @PostMapping("/clear-demo")
    public ResponseEntity<ApiResponse<Void>> clearDemoData() {
        User user = authService.getCurrentUser();
        demoDataService.clearDemoData(user);
        return ResponseEntity.ok(ApiResponse.success(null, "Demo transactions and sample budgets removed. Your personal records remain intact."));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetData() {
        User user = authService.getCurrentUser();
        demoDataService.clearAllData(user);
        return ResponseEntity.ok(ApiResponse.success(null, "All transactions and budgets have been reset successfully."));
    }
}
