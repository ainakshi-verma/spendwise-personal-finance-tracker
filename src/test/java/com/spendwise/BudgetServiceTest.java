package com.spendwise;

import com.spendwise.dto.BudgetRequest;
import com.spendwise.dto.BudgetResponse;
import com.spendwise.dto.TransactionRequest;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.exception.BadRequestException;
import com.spendwise.exception.DuplicateResourceException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.BudgetService;
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
class BudgetServiceTest {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        userA = new User();
        userA.setName("Alice");
        userA.setEmail("alice@test.com");
        userA.setPassword("pass");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setName("Bob");
        userB.setEmail("bob@test.com");
        userB.setPassword("pass");
        userB = userRepository.save(userB);
    }

    @Test
    @DisplayName("Should create a valid budget and calculate zero initial spending")
    void testCreateBudgetZeroSpending() {
        BudgetRequest request = new BudgetRequest("Food", "2026-10", new BigDecimal("3000.00"));
        BudgetResponse response = budgetService.createBudget(request, userA);

        assertNotNull(response.getId());
        assertEquals("Food", response.getCategory());
        assertEquals("2026-10", response.getMonth());
        assertEquals(new BigDecimal("3000.00"), response.getLimitAmount());
        assertEquals(BigDecimal.ZERO, response.getActualSpent());
        assertEquals(new BigDecimal("3000.00"), response.getRemainingAmount());
        assertEquals(0.0, response.getPercentageUsed());
        assertFalse(response.isExceeded());
        assertEquals("NORMAL", response.getStatus());
        assertFalse(response.isDemo());
    }

    @Test
    @DisplayName("Should reject duplicate budgets for the same category and month for the SAME user")
    void testRejectDuplicateBudgetSameUser() {
        BudgetRequest req1 = new BudgetRequest("Food", "2026-10", new BigDecimal("3000.00"));
        budgetService.createBudget(req1, userA);

        BudgetRequest req2 = new BudgetRequest("Food", "2026-10", new BigDecimal("4000.00"));
        assertThrows(DuplicateResourceException.class, () -> budgetService.createBudget(req2, userA));
    }

    @Test
    @DisplayName("User Isolation: User A and User B can have budgets for the same category and month")
    void testUserIsolationAllowedForDifferentUsers() {
        BudgetRequest req1 = new BudgetRequest("Food", "2026-10", new BigDecimal("3000.00"));
        BudgetResponse resA = budgetService.createBudget(req1, userA);

        BudgetRequest req2 = new BudgetRequest("Food", "2026-10", new BigDecimal("4500.00"));
        BudgetResponse resB = budgetService.createBudget(req2, userB);

        assertNotNull(resA.getId());
        assertNotNull(resB.getId());
        assertNotEquals(resA.getId(), resB.getId());
    }

    @Test
    @DisplayName("Should calculate budget warning and exceeded statuses accurately isolated to user spending")
    void testBudgetWarningAndExceededStatus() {
        // Budget limit: 1000.00 for userA
        budgetService.createBudget(new BudgetRequest("Transport", "2026-10", new BigDecimal("1000.00")), userA);

        // Add expense of 850.00 for userA (85% -> WARNING)
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("850.00"), "Transport", "Metro passes",
            LocalDate.of(2026, 10, 5), "UPI"
        ), userA);

        // User B also spends 500.00 on Transport - this must NOT affect User A's budget
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("500.00"), "Transport", "User B Taxi",
            LocalDate.of(2026, 10, 6), "UPI"
        ), userB);

        List<BudgetResponse> budgets = budgetService.getBudgetsForMonth(userA, "2026-10");
        assertEquals(1, budgets.size());
        BudgetResponse b = budgets.get(0);

        assertEquals(new BigDecimal("850.00"), b.getActualSpent());
        assertEquals(new BigDecimal("150.00"), b.getRemainingAmount());
        assertEquals(85.0, b.getPercentageUsed());
        assertFalse(b.isExceeded());
        assertEquals("WARNING", b.getStatus());

        // Add another expense of 300.00 for userA (total = 1150.00 -> EXCEEDED)
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("300.00"), "Transport", "Taxi trip",
            LocalDate.of(2026, 10, 8), "UPI"
        ), userA);

        List<BudgetResponse> updatedBudgets = budgetService.getBudgetsForMonth(userA, "2026-10");
        BudgetResponse exceededB = updatedBudgets.get(0);
        assertEquals(new BigDecimal("1150.00"), exceededB.getActualSpent());
        assertEquals(new BigDecimal("-150.00"), exceededB.getRemainingAmount());
        assertTrue(exceededB.isExceeded());
        assertEquals("EXCEEDED", exceededB.getStatus());
    }

    @Test
    @DisplayName("User Isolation: User B cannot update or delete User A's budget")
    void testUserIsolationOnUpdateAndDelete() {
        BudgetResponse created = budgetService.createBudget(
            new BudgetRequest("Entertainment", "2026-10", new BigDecimal("2000.00")), userA
        );

        assertThrows(ResourceNotFoundException.class, () ->
            budgetService.updateBudget(created.getId(), new BudgetRequest("Entertainment", "2026-10", new BigDecimal("5000.00")), userB)
        );

        assertThrows(ResourceNotFoundException.class, () ->
            budgetService.deleteBudget(created.getId(), userB)
        );

        // User A can delete it
        assertDoesNotThrow(() -> budgetService.deleteBudget(created.getId(), userA));
    }

    @Test
    @DisplayName("Should reject invalid month format or non-positive limit")
    void testInvalidBudgetInputs() {
        assertThrows(BadRequestException.class, () ->
            budgetService.createBudget(new BudgetRequest("Food", "invalid-month", new BigDecimal("1000.00")), userA));

        assertThrows(BadRequestException.class, () ->
            budgetService.createBudget(new BudgetRequest("Food", "2026-10", BigDecimal.ZERO), userA));

        assertThrows(BadRequestException.class, () ->
            budgetService.createBudget(new BudgetRequest("Food", "2026-10", new BigDecimal("-50.00")), userA));
    }
}
