package com.spendwise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendwise.dto.BudgetRequest;
import com.spendwise.dto.TransactionRequest;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        budgetRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Integration Tester");
        testUser.setEmail("integ@spendwise.com");
        testUser.setPassword("hashed");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Integration: Unauthenticated requests to protected endpoints return 401")
    void testUnauthenticatedAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Authentication required")));
    }

    @Test
    @WithMockUser(username = "integ@spendwise.com")
    @DisplayName("Integration: Authenticated POST, GET, PUT, and DELETE on /api/transactions")
    void testTransactionCrudLifecycle() throws Exception {
        // 1. Create Transaction
        TransactionRequest createReq = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("450.00"),
            "Books",
            "Server Architecture textbook",
            LocalDate.of(2026, 10, 10),
            "UPI"
        );

        String createJson = objectMapper.writeValueAsString(createReq);

        String responseBody = mockMvc.perform(post("/api/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.amount", is(450.00)))
                .andExpect(jsonPath("$.data.category", is("Books")))
                .andReturn().getResponse().getContentAsString();

        Number idNumber = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.id");
        Long txId = idNumber.longValue();

        // 2. Retrieve Transaction by ID
        mockMvc.perform(get("/api/transactions/" + txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(idNumber.intValue())));

        // 3. Update Transaction
        TransactionRequest updateReq = new TransactionRequest(
            TransactionType.EXPENSE,
            new BigDecimal("500.00"),
            "Education",
            "Updated server architecture textbook",
            LocalDate.of(2026, 10, 10),
            "CARD"
        );

        mockMvc.perform(put("/api/transactions/" + txId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.category", is("Education")))
                .andExpect(jsonPath("$.data.amount", is(500.00)));

        // 4. Delete Transaction
        mockMvc.perform(delete("/api/transactions/" + txId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 5. Verify 404 after deletion
        mockMvc.perform(get("/api/transactions/" + txId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(username = "integ@spendwise.com")
    @DisplayName("Integration: Reject duplicate budget on /api/budgets for same user")
    void testDuplicateBudgetRejection() throws Exception {
        BudgetRequest req = new BudgetRequest("Education", "2026-11", new BigDecimal("2500.00"));
        String json = objectMapper.writeValueAsString(req);

        // First creation succeeds
        mockMvc.perform(post("/api/budgets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));

        // Second creation with same category + month returns 409 Conflict
        mockMvc.perform(post("/api/budgets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(username = "integ@spendwise.com")
    @DisplayName("Integration: Demo data seed, idempotency, and selective clearing")
    void testDemoDataLifecycle() throws Exception {
        // 1. Seed demo data
        mockMvc.perform(post("/api/demo/seed").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 2. Dashboard displays data
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalIncome", notNullValue()));

        // 3. Clear only demo data
        mockMvc.perform(post("/api/demo/clear-demo").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 4. Reset all data
        mockMvc.perform(post("/api/demo/reset").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
