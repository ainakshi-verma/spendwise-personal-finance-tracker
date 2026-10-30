package com.spendwise;

import com.spendwise.dto.BudgetRequest;
import com.spendwise.dto.CategorySpendingDto;
import com.spendwise.dto.DashboardSummaryResponse;
import com.spendwise.dto.TransactionRequest;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.BudgetService;
import com.spendwise.service.DashboardService;
import com.spendwise.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        budgetRepository.deleteAll();
        userRepository.deleteAll();

        userA = new User();
        userA.setName("Alice");
        userA.setEmail("alice@test.com");
        userA.setPassword("secret");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setName("Bob");
        userB.setEmail("bob@test.com");
        userB.setPassword("secret");
        userB = userRepository.save(userB);
    }

    @Test
    @DisplayName("Should return clean zero metrics on empty database for user")
    void testEmptyDashboardSummary() {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(userA, "2026-10");

        assertNotNull(summary);
        assertEquals("2026-10", summary.getMonth());
        assertEquals(BigDecimal.ZERO, summary.getTotalIncome());
        assertEquals(BigDecimal.ZERO, summary.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, summary.getNetSavings());
        assertEquals(BigDecimal.ZERO, summary.getAllTimeBalance());
        assertEquals(BigDecimal.ZERO, summary.getTotalBudgetLimit());
        assertEquals(BigDecimal.ZERO, summary.getRemainingBudget());
        assertTrue(summary.getCategoryBreakdown().isEmpty());
        assertTrue(summary.getRecentTransactions().isEmpty());
    }

    @Test
    @DisplayName("Should calculate monthly summary and category distribution correctly isolated per user")
    void testDashboardCalculations() {
        // User A - Income in 2026-10: 20,000
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.INCOME, new BigDecimal("20000.00"), "Salary", "October stipend",
            LocalDate.of(2026, 10, 1), "BANK_TRANSFER"
        ), userA);

        // User A - Income in 2026-09 (prior month): 15,000
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.INCOME, new BigDecimal("15000.00"), "Salary", "September stipend",
            LocalDate.of(2026, 9, 1), "BANK_TRANSFER"
        ), userA);

        // User A - Expenses in 2026-10: Food (3,000), Transport (1,000) -> Total = 4,000
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("3000.00"), "Food", "Groceries",
            LocalDate.of(2026, 10, 5), "UPI"
        ), userA);
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("1000.00"), "Transport", "Metro",
            LocalDate.of(2026, 10, 10), "UPI"
        ), userA);

        // User A - Budget for Food: 5,000
        budgetService.createBudget(new BudgetRequest("Food", "2026-10", new BigDecimal("5000.00")), userA);

        // User B has transactions that must NOT bleed into User A's dashboard
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.INCOME, new BigDecimal("99000.00"), "Salary", "User B income",
            LocalDate.of(2026, 10, 1), "BANK_TRANSFER"
        ), userB);
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("50000.00"), "Luxury", "User B luxury expense",
            LocalDate.of(2026, 10, 2), "CARD"
        ), userB);

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(userA, "2026-10");

        // Monthly metrics for User A
        assertEquals(new BigDecimal("20000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("4000.00"), summary.getTotalExpenses());
        assertEquals(new BigDecimal("16000.00"), summary.getNetSavings());

        // All-Time balance for User A = (20000 + 15000) - 4000 = 31,000
        assertEquals(new BigDecimal("31000.00"), summary.getAllTimeBalance());

        // Budget metrics for User A
        assertEquals(new BigDecimal("5000.00"), summary.getTotalBudgetLimit());
        assertEquals(new BigDecimal("3000.00"), summary.getTotalBudgetSpent());
        assertEquals(new BigDecimal("2000.00"), summary.getRemainingBudget());

        // Category breakdown should only include EXPENSE (Food 75%, Transport 25%)
        List<CategorySpendingDto> categories = summary.getCategoryBreakdown();
        assertEquals(2, categories.size());

        CategorySpendingDto topCategory = categories.get(0);
        assertEquals("Food", topCategory.getCategory());
        assertEquals(new BigDecimal("3000.00"), topCategory.getAmount());
        assertEquals(75.0, topCategory.getPercentage());
    }
}
