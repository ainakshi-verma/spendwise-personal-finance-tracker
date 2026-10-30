package com.spendwise;

import com.spendwise.dto.TransactionRequest;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.DemoDataService;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class DemoDataServiceTest {

    @Autowired
    private DemoDataService demoDataService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        budgetRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("Demo Tester");
        user.setEmail("tester@test.com");
        user.setPassword("secret");
        user = userRepository.save(user);
    }

    @Test
    @DisplayName("Demo Seeding Idempotency: Multiple clicks do not insert duplicates")
    void testDemoSeedIdempotency() {
        // First seeding creates demo records
        int seededFirst = demoDataService.seedDemoData(user);
        assertTrue(seededFirst > 0, "First seeding should insert records");

        long txCountAfterFirst = transactionRepository.countByUserAndIsDemoTrue(user);
        assertEquals(seededFirst, txCountAfterFirst);

        // Second seeding must detect existing demo data and return 0 (idempotent, no duplicates)
        int seededSecond = demoDataService.seedDemoData(user);
        assertEquals(0, seededSecond, "Second seeding should be idempotent and insert 0 records");

        long txCountAfterSecond = transactionRepository.countByUserAndIsDemoTrue(user);
        assertEquals(seededFirst, txCountAfterSecond, "Total demo transactions should remain unchanged");
    }

    @Test
    @DisplayName("Selective Clearing: Deleting demo data preserves personal transactions")
    void testSelectiveDemoClearing() {
        // 1. User enters personal real transaction
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("1200.00"),
            "Health",
            "Personal dental checkup",
            LocalDate.of(2026, 10, 15),
            "UPI"
        ), user);

        // 2. User seeds demo data
        int seeded = demoDataService.seedDemoData(user);
        assertTrue(seeded > 0);

        // Total count = 1 personal + seeded demo records
        assertEquals(seeded + 1, transactionRepository.count());

        // 3. User chooses 'Clear Demo Data'
        demoDataService.clearDemoData(user);

        // Demo records are 0
        assertEquals(0, transactionRepository.countByUserAndIsDemoTrue(user));

        // Personal record is preserved intact!
        assertEquals(1, transactionRepository.count());
        assertEquals("Personal dental checkup", transactionRepository.findAll().get(0).getDescription());
        assertFalse(transactionRepository.findAll().get(0).isDemo());
    }

    @Test
    @DisplayName("Reset All Data: Clears both personal and demo data for this user")
    void testResetAllData() {
        // 1 personal transaction
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.INCOME,
            new BigDecimal("5000.00"),
            "Salary",
            "Real personal salary",
            LocalDate.of(2026, 10, 1),
            "BANK_TRANSFER"
        ), user);

        // Seed demo data
        demoDataService.seedDemoData(user);

        // Reset all
        demoDataService.clearAllData(user);

        assertEquals(0, transactionRepository.countByUserAndIsDemoTrue(user));
        assertEquals(0, transactionRepository.count());
        assertEquals(0, budgetRepository.count());
    }
}
