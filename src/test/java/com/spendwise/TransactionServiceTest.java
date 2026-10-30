package com.spendwise;

import com.spendwise.dto.TransactionRequest;
import com.spendwise.dto.TransactionResponse;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.exception.BadRequestException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class TransactionServiceTest {

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
        userA.setName("User Alpha");
        userA.setEmail("alpha@test.com");
        userA.setPassword("secret");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setName("User Beta");
        userB.setEmail("beta@test.com");
        userB.setPassword("secret");
        userB = userRepository.save(userB);
    }

    @Test
    @DisplayName("Should create a valid transaction successfully")
    void testCreateValidTransaction() {
        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("750.00"),
            "Food",
            "College canteen lunch",
            LocalDate.of(2026, 10, 15),
            "UPI"
        );

        TransactionResponse response = transactionService.createTransaction(request, userA);

        assertNotNull(response.getId());
        assertEquals(TransactionType.EXPENSE, response.getType());
        assertEquals(new BigDecimal("750.00"), response.getAmount());
        assertEquals("Food", response.getCategory());
        assertEquals("College canteen lunch", response.getDescription());
        assertEquals(LocalDate.of(2026, 10, 15), response.getTransactionDate());
        assertEquals("UPI", response.getPaymentMethod());
        assertNotNull(response.getCreatedAt());
        assertFalse(response.isDemo(), "User created transaction must have isDemo=false");
    }

    @Test
    @DisplayName("Should reject invalid or zero transaction amounts")
    void testRejectInvalidAmount() {
        TransactionRequest zeroRequest = new TransactionRequest(
            TransactionType.EXPENSE,
            BigDecimal.ZERO,
            "Food",
            "Free snack",
            LocalDate.now(),
            "CASH"
        );

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(zeroRequest, userA));

        TransactionRequest negativeRequest = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("-100.00"),
            "Transport",
            "Invalid refund",
            LocalDate.now(),
            "CASH"
        );

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(negativeRequest, userA));
    }

    @Test
    @DisplayName("Should retrieve an existing transaction by ID for owner")
    void testGetTransactionById() {
        TransactionRequest request = new TransactionRequest(
            TransactionType.INCOME,
            new BigDecimal("10000.00"),
            "Salary",
            "Monthly stipend",
            LocalDate.of(2026, 10, 1),
            "BANK_TRANSFER"
        );

        TransactionResponse created = transactionService.createTransaction(request, userA);
        TransactionResponse found = transactionService.getTransactionById(created.getId(), userA);

        assertEquals(created.getId(), found.getId());
        assertEquals("Salary", found.getCategory());
    }

    @Test
    @DisplayName("User Isolation: User B cannot view User A's transaction")
    void testUserIsolationOnGet() {
        TransactionRequest request = new TransactionRequest(
            TransactionType.INCOME,
            new BigDecimal("5000.00"),
            "Salary",
            "User A Private Income",
            LocalDate.of(2026, 10, 1),
            "BANK_TRANSFER"
        );

        TransactionResponse created = transactionService.createTransaction(request, userA);
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(created.getId(), userB));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction ID does not exist")
    void testGetMissingTransactionThrowsNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(999999L, userA));
    }

    @Test
    @DisplayName("Should update an existing transaction successfully")
    void testUpdateTransaction() {
        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("500.00"),
            "Books",
            "Old book description",
            LocalDate.of(2026, 10, 10),
            "CASH"
        );
        TransactionResponse created = transactionService.createTransaction(request, userA);

        TransactionRequest updateRequest = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("650.00"),
            "Education",
            "Updated textbook description",
            LocalDate.of(2026, 10, 12),
            "UPI"
        );
        TransactionResponse updated = transactionService.updateTransaction(created.getId(), updateRequest, userA);

        assertEquals(created.getId(), updated.getId());
        assertEquals(new BigDecimal("650.00"), updated.getAmount());
        assertEquals("Education", updated.getCategory());
        assertEquals("Updated textbook description", updated.getDescription());
        assertEquals("UPI", updated.getPaymentMethod());
    }

    @Test
    @DisplayName("User Isolation: User B cannot update User A's transaction")
    void testUserIsolationOnUpdate() {
        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("500.00"),
            "Books",
            "User A book",
            LocalDate.of(2026, 10, 10),
            "CASH"
        );
        TransactionResponse created = transactionService.createTransaction(request, userA);

        TransactionRequest updateRequest = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("650.00"),
            "Education",
            "Hacked by User B",
            LocalDate.of(2026, 10, 12),
            "UPI"
        );

        assertThrows(ResourceNotFoundException.class, () ->
            transactionService.updateTransaction(created.getId(), updateRequest, userB));
    }

    @Test
    @DisplayName("Should delete an existing transaction successfully")
    void testDeleteTransaction() {
        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("300.00"),
            "Transport",
            "Bus ticket",
            LocalDate.now(),
            "CASH"
        );
        TransactionResponse created = transactionService.createTransaction(request, userA);
        Long id = created.getId();

        transactionService.deleteTransaction(id, userA);

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(id, userA));
    }

    @Test
    @DisplayName("User Isolation: User B cannot delete User A's transaction")
    void testUserIsolationOnDelete() {
        TransactionRequest request = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("300.00"),
            "Transport",
            "Bus ticket",
            LocalDate.now(),
            "CASH"
        );
        TransactionResponse created = transactionService.createTransaction(request, userA);
        Long id = created.getId();

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(id, userB));
        assertNotNull(transactionService.getTransactionById(id, userA));
    }

    @Test
    @DisplayName("Should filter and paginate transactions accurately per user")
    void testFilterAndPaginateTransactions() {
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.INCOME, new BigDecimal("12000.00"), "Allowance", "Monthly stipend",
            LocalDate.of(2026, 10, 1), "BANK_TRANSFER"
        ), userA);
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("800.00"), "Food", "College mess meal",
            LocalDate.of(2026, 10, 5), "UPI"
        ), userA);
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("1500.00"), "Education", "Java textbook purchase",
            LocalDate.of(2026, 10, 10), "CARD"
        ), userA);

        // Transaction for User B should NOT be seen by User A
        transactionService.createTransaction(new TransactionRequest(
            TransactionType.EXPENSE, new BigDecimal("9999.00"), "Food", "User B luxury meal",
            LocalDate.of(2026, 10, 5), "UPI"
        ), userB);

        // Filter by EXPENSE for User A
        Page<TransactionResponse> expenses = transactionService.getTransactions(
            userA, TransactionType.EXPENSE, null, null, null, null, PageRequest.of(0, 10)
        );
        assertEquals(2, expenses.getTotalElements());

        // Filter by category "Food" for User A
        Page<TransactionResponse> foodOnly = transactionService.getTransactions(
            userA, null, "Food", null, null, null, PageRequest.of(0, 10)
        );
        assertEquals(1, foodOnly.getTotalElements());
        assertEquals("Food", foodOnly.getContent().get(0).getCategory());

        // Search text "Java" for User A
        Page<TransactionResponse> searchResult = transactionService.getTransactions(
            userA, null, null, null, null, "Java", PageRequest.of(0, 10)
        );
        assertEquals(1, searchResult.getTotalElements());
        assertTrue(searchResult.getContent().get(0).getDescription().contains("Java"));

        // User B has 1 expense
        Page<TransactionResponse> userBExpenses = transactionService.getTransactions(
            userB, null, null, null, null, null, PageRequest.of(0, 10)
        );
        assertEquals(1, userBExpenses.getTotalElements());
    }
}
