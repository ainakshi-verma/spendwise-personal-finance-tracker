package com.spendwise;

import com.spendwise.dto.CategorySpendingDto;
import com.spendwise.dto.MonthlyReportResponse;
import com.spendwise.dto.MonthlyTrendDto;
import com.spendwise.dto.TransactionRequest;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.ReportService;
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
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

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
        transactionRepository.deleteAll();
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
    @DisplayName("Should generate monthly report with prior month delta comparison isolated to user")
    void testMonthlyReportWithPriorMonthComparison() {
        // September (prior month) for User A
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.INCOME, new BigDecimal("10000.00"), "Salary", "Sep Income",
            LocalDate.of(2026, 9, 1), "BANK_TRANSFER"
        ), userA);
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("5000.00"), "Food", "Sep Food",
            LocalDate.of(2026, 9, 10), "UPI"
        ), userA);

        // October (target month) for User A
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.INCOME, new BigDecimal("15000.00"), "Salary", "Oct Income",
            LocalDate.of(2026, 10, 1), "BANK_TRANSFER"
        ), userA);
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("6000.00"), "Food", "Oct Food",
            LocalDate.of(2026, 10, 5), "UPI"
        ), userA);
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("2000.00"), "Education", "Oct Books",
            LocalDate.of(2026, 10, 12), "CARD"
        ), userA);

        // Transactions for User B must NOT affect User A's report
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("40000.00"), "Food", "User B luxury dining",
            LocalDate.of(2026, 10, 5), "UPI"
        ), userB);

        MonthlyReportResponse report = reportService.getMonthlyReport(userA, "2026-10");

        assertEquals(new BigDecimal("15000.00"), report.getTotalIncome());
        assertEquals(new BigDecimal("8000.00"), report.getTotalExpenses());
        assertEquals(new BigDecimal("7000.00"), report.getNetSavings());

        assertTrue(report.isHasPreviousMonthData());
        assertEquals(new BigDecimal("10000.00"), report.getPrevTotalIncome());
        assertEquals(new BigDecimal("5000.00"), report.getPrevTotalExpenses());

        // Deltas: income +5000 (50%), expense +3000 (60%)
        assertEquals(new BigDecimal("5000.00"), report.getIncomeChange());
        assertEquals(50.0, report.getIncomeChangePct());
        assertEquals(new BigDecimal("3000.00"), report.getExpenseChange());
        assertEquals(60.0, report.getExpenseChangePct());

        // Highest spending category should be Food (6000.00)
        assertEquals("Food", report.getHighestSpendingCategory());
        assertEquals(new BigDecimal("6000.00"), report.getHighestSpendingAmount());
    }

    @Test
    @DisplayName("Should generate monthly trend series for requested number of months per user")
    void testMonthlyTrends() {
        List<MonthlyTrendDto> trends = reportService.getMonthlyTrends(userA, 6);
        assertEquals(6, trends.size());
        assertNotNull(trends.get(0).getMonth());
        assertNotNull(trends.get(0).getMonthLabel());
    }
}
